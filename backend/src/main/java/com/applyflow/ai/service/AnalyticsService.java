package com.applyflow.ai.service;

import com.applyflow.ai.common.DomainEnums;
import com.applyflow.ai.dto.ApiDtos;
import com.applyflow.ai.entity.AiInsightEntity;
import com.applyflow.ai.entity.InterviewEntity;
import com.applyflow.ai.entity.JobApplicationEntity;
import com.applyflow.ai.entity.ResumeEntity;
import com.applyflow.ai.entity.UserEntity;
import com.applyflow.ai.repository.AiInsightRepository;
import com.applyflow.ai.repository.ApplicationRepository;
import com.applyflow.ai.repository.InterviewRepository;
import com.applyflow.ai.repository.ResumeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final ResumeRepository resumeRepository;
    private final AiInsightRepository aiInsightRepository;
    private final CurrentUserService currentUserService;
    private final AiAnalysisService aiAnalysisService;
    private final ObjectMapper objectMapper;

    public AnalyticsService(ApplicationRepository applicationRepository, InterviewRepository interviewRepository, ResumeRepository resumeRepository, AiInsightRepository aiInsightRepository, CurrentUserService currentUserService, AiAnalysisService aiAnalysisService, ObjectMapper objectMapper) {
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.resumeRepository = resumeRepository;
        this.aiInsightRepository = aiInsightRepository;
        this.currentUserService = currentUserService;
        this.aiAnalysisService = aiAnalysisService;
        this.objectMapper = objectMapper;
    }

    public ApiDtos.DashboardResponse dashboard() {
        ApiDtos.ApplicationStatsResponse stats = applicationServiceStats();
        List<ApiDtos.TimePoint> timeline = applicationTimeline();
        List<ApiDtos.CategoryPoint> platforms = platformBreakdown();
        List<ApiDtos.InsightResponse> insights = ensureInsights();
        List<ApiDtos.InterviewResponse> interviews = upcomingInterviews();
        return new ApiDtos.DashboardResponse(stats, timeline, platforms, insights, interviews);
    }

    public List<ApiDtos.InsightResponse> listInsights() {
        UserEntity user = currentUserService.currentUser();
        return aiInsightRepository.findTop10ByUserOrderByGeneratedAtDesc(user).stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<ApiDtos.InsightResponse> refreshInsights() {
        UserEntity user = currentUserService.currentUser();
        List<JobApplicationEntity> apps = applicationRepository.findByUserOrderByCreatedAtDesc(user);
        List<ResumeEntity> resumes = resumeRepository.findByUserOrderByCreatedAtDesc(user);
        List<ApiDtos.InsightResponse> responses = new ArrayList<>();

        if (!apps.isEmpty()) {
            Map<String, Long> byPlatform = apps.stream().collect(Collectors.groupingBy(JobApplicationEntity::getSourcePlatform, LinkedHashMap::new, Collectors.counting()));
            String bestPlatform = byPlatform.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("Unknown");
            AiAnalysisService.InsightDraft platformDraft = aiAnalysisService.generateInsightDraft(
                    List.of("Best-performing platform appears to be " + bestPlatform + ".", "Applications by platform: " + byPlatform),
                    "application performance");
            responses.add(saveInsight(user, DomainEnums.InsightType.PLATFORM_PERFORMANCE, platformDraft.title(), platformDraft.summary(), platformDraft.recommendations(), platformDraft.confidence(), null));
        }

        if (!resumes.isEmpty()) {
            Map<String, Long> missingSkillCounts = new LinkedHashMap<>();
            for (ResumeEntity resume : resumes) {
                parseStringList(resume.getMissingKeywordsJson()).forEach(keyword -> missingSkillCounts.merge(keyword.toLowerCase(), 1L, Long::sum));
            }
            String mostMissing = missingSkillCounts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("N/A");
            AiAnalysisService.InsightDraft resumeDraft = aiAnalysisService.generateInsightDraft(
                    List.of("Most commonly missing skill: " + mostMissing + ".", "Missing skills: " + missingSkillCounts),
                    "resume optimization");
            responses.add(saveInsight(user, DomainEnums.InsightType.RESUME_GAP, resumeDraft.title(), resumeDraft.summary(), resumeDraft.recommendations(), resumeDraft.confidence(), null));
        }
        return responses;
    }

    public List<ApiDtos.TimePoint> applicationTimeline() {
        UserEntity user = currentUserService.currentUser();
        return applicationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .collect(Collectors.groupingBy(app -> YearMonth.from(app.getApplicationDate()), LinkedHashMap::new, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new ApiDtos.TimePoint(entry.getKey().toString(), entry.getValue()))
                .toList();
    }

    public List<ApiDtos.CategoryPoint> platformBreakdown() {
        UserEntity user = currentUserService.currentUser();
        return applicationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .collect(Collectors.groupingBy(JobApplicationEntity::getSourcePlatform, LinkedHashMap::new, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(entry -> new ApiDtos.CategoryPoint(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<ApiDtos.InterviewResponse> upcomingInterviews() {
        UserEntity user = currentUserService.currentUser();
        return interviewRepository.findTop10ByApplicationUserOrderByScheduledAtAsc(user).stream().map(this::toInterviewResponse).toList();
    }

    private ApiDtos.ApplicationStatsResponse applicationServiceStats() {
        UserEntity user = currentUserService.currentUser();
        long total = applicationRepository.countByUser(user);
        long saved = applicationRepository.countByUserAndStatus(user, DomainEnums.ApplicationStatus.SAVED);
        long applied = applicationRepository.countByUserAndStatus(user, DomainEnums.ApplicationStatus.APPLIED);
        long assessment = applicationRepository.countByUserAndStatus(user, DomainEnums.ApplicationStatus.ONLINE_ASSESSMENT);
        long interview = applicationRepository.countByUserAndStatus(user, DomainEnums.ApplicationStatus.INTERVIEW);
        long offer = applicationRepository.countByUserAndStatus(user, DomainEnums.ApplicationStatus.OFFER);
        long rejected = applicationRepository.countByUserAndStatus(user, DomainEnums.ApplicationStatus.REJECTED);
        double interviewRate = total == 0 ? 0 : Math.round((interview * 10000.0 / total)) / 100.0;
        double offerRate = total == 0 ? 0 : Math.round((offer * 10000.0 / total)) / 100.0;
        double rejectionRate = total == 0 ? 0 : Math.round((rejected * 10000.0 / total)) / 100.0;
        Map<String, Long> byPlatform = applicationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .collect(Collectors.groupingBy(JobApplicationEntity::getSourcePlatform, LinkedHashMap::new, Collectors.counting()));
        return new ApiDtos.ApplicationStatsResponse(total, saved, applied, assessment, interview, offer, rejected, interviewRate, offerRate, rejectionRate, byPlatform);
    }

    private List<ApiDtos.InsightResponse> ensureInsights() {
        List<ApiDtos.InsightResponse> existing = listInsights();
        if (!existing.isEmpty()) {
            return existing;
        }
        return refreshInsights();
    }

    private ApiDtos.InsightResponse saveInsight(UserEntity user, DomainEnums.InsightType type, String title, String summary, List<String> recommendations, double confidence, JobApplicationEntity application) {
        AiInsightEntity saved = aiInsightRepository.save(AiInsightEntity.builder()
                .user(user)
                .application(application)
                .insightType(type)
                .title(title)
                .summary(summary)
                .recommendationsJson(writeJson(recommendations))
                .confidence(confidence)
                .generatedAt(java.time.Instant.now())
                .build());
        return toResponse(saved);
    }

    private ApiDtos.InsightResponse toResponse(AiInsightEntity entity) {
        return new ApiDtos.InsightResponse(
                entity.getId(),
                entity.getInsightType(),
                entity.getTitle(),
                entity.getSummary(),
                parseStringList(entity.getRecommendationsJson()),
                entity.getConfidence(),
                entity.getGeneratedAt());
    }

    private ApiDtos.InterviewResponse toInterviewResponse(InterviewEntity entity) {
        return new ApiDtos.InterviewResponse(
                entity.getId(),
                entity.getApplication().getId(),
                entity.getApplication().getCompany(),
                entity.getApplication().getRoleName(),
                entity.getRoundName(),
                entity.getScheduledAt(),
                entity.getLocation(),
                entity.getInterviewType(),
                entity.getFeedback(),
                entity.getNotes(),
                entity.getStatus(),
                entity.isReminderSent(),
                entity.getCreatedAt());
    }

    private String writeJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception exception) {
            return "[]";
        }
    }

    private List<String> parseStringList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception exception) {
            return List.of();
        }
    }
}
