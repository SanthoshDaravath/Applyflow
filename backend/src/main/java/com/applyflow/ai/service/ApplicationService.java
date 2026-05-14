package com.applyflow.ai.service;

import com.applyflow.ai.common.DomainEnums;
import com.applyflow.ai.dto.ApiDtos;
import com.applyflow.ai.entity.JobApplicationEntity;
import com.applyflow.ai.entity.UserEntity;
import com.applyflow.ai.exception.ApiException;
import com.applyflow.ai.repository.ApplicationRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;

    public ApplicationService(ApplicationRepository applicationRepository, CurrentUserService currentUserService, ObjectMapper objectMapper) {
        this.applicationRepository = applicationRepository;
        this.currentUserService = currentUserService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ApiDtos.ApplicationResponse create(ApiDtos.ApplicationRequest request) {
        UserEntity user = currentUserService.currentUser();
        JobApplicationEntity entity = JobApplicationEntity.builder()
                .user(user)
                .company(request.company())
                .roleName(request.role())
                .salary(request.salary())
                .location(request.location())
                .sourcePlatform(defaultIfBlank(request.sourcePlatform(), "Manual"))
                .applicationDate(Optional.ofNullable(request.applicationDate()).orElse(LocalDate.now()))
                .status(Optional.ofNullable(request.status()).orElse(DomainEnums.ApplicationStatus.SAVED))
                .notes(request.notes())
                .jobUrl(request.jobUrl())
                .resumeSnapshot(request.resumeSnapshot())
                .build();
        return toResponse(applicationRepository.save(entity));
    }

    @Transactional
    public ApiDtos.ApplicationResponse update(UUID id, ApiDtos.ApplicationRequest request) {
        JobApplicationEntity entity = getOwnedApplication(id);
        entity.setCompany(request.company());
        entity.setRoleName(request.role());
        entity.setSalary(request.salary());
        entity.setLocation(request.location());
        entity.setSourcePlatform(defaultIfBlank(request.sourcePlatform(), entity.getSourcePlatform()));
        entity.setApplicationDate(Optional.ofNullable(request.applicationDate()).orElse(entity.getApplicationDate()));
        entity.setStatus(Optional.ofNullable(request.status()).orElse(entity.getStatus()));
        entity.setNotes(request.notes());
        entity.setJobUrl(request.jobUrl());
        entity.setResumeSnapshot(request.resumeSnapshot());
        return toResponse(applicationRepository.save(entity));
    }

    @Transactional
    public ApiDtos.ApplicationResponse updateStatus(UUID id, ApiDtos.StatusUpdateRequest request) {
        JobApplicationEntity entity = getOwnedApplication(id);
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        if (StringUtils.hasText(request.notes())) {
            entity.setNotes(request.notes());
        }
        return toResponse(applicationRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        JobApplicationEntity entity = getOwnedApplication(id);
        applicationRepository.delete(entity);
    }

    public ApiDtos.ApplicationResponse getById(UUID id) {
        return toResponse(getOwnedApplication(id));
    }

    public ApiDtos.PageResponse<ApiDtos.ApplicationResponse> list(String query, DomainEnums.ApplicationStatus status, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);
        UserEntity user = currentUserService.currentUser();
        List<JobApplicationEntity> all = applicationRepository.findByUserOrderByCreatedAtDesc(user);
        Predicate<JobApplicationEntity> filters = app -> true;
        if (status != null) {
            filters = filters.and(app -> app.getStatus() == status);
        }
        if (StringUtils.hasText(query)) {
            String normalized = query.toLowerCase(Locale.ROOT);
            filters = filters.and(app -> contains(app.getCompany(), normalized) || contains(app.getRoleName(), normalized) || contains(app.getNotes(), normalized) || contains(app.getLocation(), normalized));
        }
        List<ApiDtos.ApplicationResponse> content = all.stream().filter(filters).map(this::toResponse).toList();
        int fromIndex = Math.min(safePage * safeSize, content.size());
        int toIndex = Math.min(fromIndex + safeSize, content.size());
        List<ApiDtos.ApplicationResponse> items = content.subList(fromIndex, toIndex);
        return new ApiDtos.PageResponse<>(items, safePage, safeSize, content.size(), (int) Math.ceil(content.size() / (double) safeSize));
    }

    public List<ApiDtos.KanbanColumnResponse> kanban() {
        UserEntity user = currentUserService.currentUser();
        List<JobApplicationEntity> apps = applicationRepository.findByUserOrderByCreatedAtDesc(user);
        return List.of(
                column("SAVED", apps, DomainEnums.ApplicationStatus.SAVED),
                column("APPLIED", apps, DomainEnums.ApplicationStatus.APPLIED),
                column("ONLINE_ASSESSMENT", apps, DomainEnums.ApplicationStatus.ONLINE_ASSESSMENT),
                column("INTERVIEW", apps, DomainEnums.ApplicationStatus.INTERVIEW),
                column("OFFER", apps, DomainEnums.ApplicationStatus.OFFER),
                column("REJECTED", apps, DomainEnums.ApplicationStatus.REJECTED));
    }

    public ApiDtos.ApplicationStatsResponse stats() {
        UserEntity user = currentUserService.currentUser();
        long total = applicationRepository.countByUser(user);
        long saved = applicationRepository.countByUserAndStatus(user, DomainEnums.ApplicationStatus.SAVED);
        long applied = applicationRepository.countByUserAndStatus(user, DomainEnums.ApplicationStatus.APPLIED);
        long assessment = applicationRepository.countByUserAndStatus(user, DomainEnums.ApplicationStatus.ONLINE_ASSESSMENT);
        long interview = applicationRepository.countByUserAndStatus(user, DomainEnums.ApplicationStatus.INTERVIEW);
        long offer = applicationRepository.countByUserAndStatus(user, DomainEnums.ApplicationStatus.OFFER);
        long rejected = applicationRepository.countByUserAndStatus(user, DomainEnums.ApplicationStatus.REJECTED);
        double interviewRate = total == 0 ? 0 : roundPercent(interview * 100.0 / total);
        double offerRate = total == 0 ? 0 : roundPercent(offer * 100.0 / total);
        double rejectionRate = total == 0 ? 0 : roundPercent(rejected * 100.0 / total);
        Map<String, Long> byPlatform = applicationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .collect(Collectors.groupingBy(JobApplicationEntity::getSourcePlatform, LinkedHashMap::new, Collectors.counting()));
        return new ApiDtos.ApplicationStatsResponse(total, saved, applied, assessment, interview, offer, rejected, interviewRate, offerRate, rejectionRate, byPlatform);
    }

    public List<ApiDtos.TimePoint> timeline() {
        UserEntity user = currentUserService.currentUser();
        List<JobApplicationEntity> apps = applicationRepository.findByUserOrderByCreatedAtDesc(user);
        Map<YearMonth, Long> byMonth = apps.stream().collect(Collectors.groupingBy(app -> YearMonth.from(app.getApplicationDate()), LinkedHashMap::new, Collectors.counting()));
        return byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new ApiDtos.TimePoint(entry.getKey().toString(), entry.getValue()))
                .toList();
    }

    public List<ApiDtos.CategoryPoint> platformBreakdown() {
        UserEntity user = currentUserService.currentUser();
        return applicationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .collect(Collectors.groupingBy(JobApplicationEntity::getSourcePlatform, LinkedHashMap::new, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> new ApiDtos.CategoryPoint(entry.getKey(), entry.getValue()))
                .toList();
    }

    private ApiDtos.KanbanColumnResponse column(String label, List<JobApplicationEntity> apps, DomainEnums.ApplicationStatus status) {
        List<ApiDtos.ApplicationResponse> items = apps.stream()
                .filter(app -> app.getStatus() == status)
                .sorted(Comparator.comparing(JobApplicationEntity::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
        return new ApiDtos.KanbanColumnResponse(label, items);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private JobApplicationEntity getOwnedApplication(UUID id) {
        UserEntity user = currentUserService.currentUser();
        JobApplicationEntity entity = applicationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "APPLICATION_NOT_FOUND", "Application not found"));
        if (!entity.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You cannot modify this application");
        }
        return entity;
    }

    private ApiDtos.ApplicationResponse toResponse(JobApplicationEntity entity) {
        return new ApiDtos.ApplicationResponse(
                entity.getId(),
                entity.getCompany(),
                entity.getRoleName(),
                entity.getSalary(),
                entity.getLocation(),
                entity.getSourcePlatform(),
                entity.getApplicationDate(),
                entity.getStatus(),
                entity.getNotes(),
                entity.getJobUrl(),
                entity.getResumeSnapshot(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private double roundPercent(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
