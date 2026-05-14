package com.applyflow.ai.service;

import com.applyflow.ai.common.DomainEnums;
import com.applyflow.ai.dto.ApiDtos;
import com.applyflow.ai.entity.JobApplicationEntity;
import com.applyflow.ai.entity.ResumeEntity;
import com.applyflow.ai.entity.UserEntity;
import com.applyflow.ai.exception.ApiException;
import com.applyflow.ai.repository.ApplicationRepository;
import com.applyflow.ai.repository.ResumeRepository;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ApplicationRepository applicationRepository;
    private final CurrentUserService currentUserService;
    private final AiAnalysisService aiAnalysisService;

    public ResumeService(ResumeRepository resumeRepository, ApplicationRepository applicationRepository, CurrentUserService currentUserService, AiAnalysisService aiAnalysisService) {
        this.resumeRepository = resumeRepository;
        this.applicationRepository = applicationRepository;
        this.currentUserService = currentUserService;
        this.aiAnalysisService = aiAnalysisService;
    }

    @Transactional
    public ApiDtos.ResumeResponse upload(MultipartFile file, String title, String jobDescription, String extractedText, UUID applicationId) {
        UserEntity user = currentUserService.currentUser();
        JobApplicationEntity application = null;
        if (applicationId != null) {
            application = applicationRepository.findById(applicationId)
                    .filter(app -> app.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "APPLICATION_NOT_FOUND", "Application not found for this resume"));
        }
        String text = resolveResumeText(file, extractedText);
        ApiDtos.ResumeAnalysisResponse analysis = analyze(text, jobDescription);
        ResumeEntity entity = ResumeEntity.builder()
                .user(user)
                .application(application)
                .title(StringUtils.hasText(title) ? title : (file != null ? file.getOriginalFilename() : "Resume"))
                .fileName(file != null ? file.getOriginalFilename() : null)
                .contentType(file != null ? file.getContentType() : null)
                .storagePath(null)
                .fileData(readBytes(file))
                .source(DomainEnums.ResumeSource.UPLOADED)
                .jobDescription(jobDescription)
                .resumeText(text)
                .atsScore(analysis.atsScore())
                .matchedKeywordsJson(asJson(analysis.matchedKeywords()))
                .missingKeywordsJson(asJson(analysis.missingKeywords()))
                .suggestionsJson(asJson(analysis.suggestions()))
                .build();
        return toResponse(resumeRepository.save(entity));
    }

    public ApiDtos.ResumeAnalysisResponse analyze(String resumeText, String jobDescription) {
        AiAnalysisService.ResumeAnalysis analysis = aiAnalysisService.analyzeResume(resumeText, jobDescription);
        return new ApiDtos.ResumeAnalysisResponse(
                analysis.atsScore(),
                analysis.matchedKeywords(),
                analysis.missingKeywords(),
                analysis.suggestions(),
                analysis.summary());
    }

    public List<ApiDtos.ResumeResponse> listMine() {
        return resumeRepository.findByUserOrderByCreatedAtDesc(currentUserService.currentUser())
                .stream().map(this::toResponse).toList();
    }

    public List<ApiDtos.ResumeResponse> topResumes() {
        return resumeRepository.findTop5ByUserOrderByAtsScoreDescCreatedAtDesc(currentUserService.currentUser())
                .stream().map(this::toResponse).toList();
    }

    private ApiDtos.ResumeResponse toResponse(ResumeEntity entity) {
        return new ApiDtos.ResumeResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getFileName(),
                entity.getJobDescription(),
                entity.getResumeText(),
                entity.getAtsScore(),
                parseJsonList(entity.getMatchedKeywordsJson()),
                parseJsonList(entity.getMissingKeywordsJson()),
                parseJsonList(entity.getSuggestionsJson()),
                entity.getSource(),
                entity.getCreatedAt());
    }

    private String resolveResumeText(MultipartFile file, String extractedText) {
        if (StringUtils.hasText(extractedText)) {
            return extractedText;
        }
        if (file == null || file.isEmpty()) {
            return "";
        }
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "RESUME_UPLOAD_FAILED", "Unable to read uploaded resume");
        }
    }

    private byte[] readBytes(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "RESUME_UPLOAD_FAILED", "Unable to read uploaded resume bytes");
        }
    }

    private String asJson(List<String> values) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(values);
        } catch (Exception exception) {
            return "[]";
        }
    }

    private List<String> parseJsonList(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, new com.fasterxml.jackson.core.type.TypeReference<>() {});
        } catch (Exception exception) {
            return List.of();
        }
    }
}
