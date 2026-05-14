package com.applyflow.ai.service;

import com.applyflow.ai.common.DomainEnums;
import com.applyflow.ai.dto.ApiDtos;
import com.applyflow.ai.entity.InterviewEntity;
import com.applyflow.ai.entity.JobApplicationEntity;
import com.applyflow.ai.entity.UserEntity;
import com.applyflow.ai.exception.ApiException;
import com.applyflow.ai.repository.ApplicationRepository;
import com.applyflow.ai.repository.InterviewRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public InterviewService(InterviewRepository interviewRepository, ApplicationRepository applicationRepository, CurrentUserService currentUserService, NotificationService notificationService) {
        this.interviewRepository = interviewRepository;
        this.applicationRepository = applicationRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ApiDtos.InterviewResponse create(ApiDtos.InterviewRequest request) {
        JobApplicationEntity application = getOwnedApplication(request.applicationId());
        InterviewEntity interview = interviewRepository.save(InterviewEntity.builder()
                .application(application)
                .roundName(request.roundName())
                .scheduledAt(request.scheduledAt())
                .location(request.location())
                .interviewType(request.interviewType())
                .feedback(request.feedback())
                .notes(request.notes())
                .status(request.status() != null ? request.status() : DomainEnums.InterviewStatus.SCHEDULED)
                .reminderSent(false)
                .build());
        notificationService.scheduleInterviewReminder(interview, application);
        return toResponse(interview);
    }

    @Transactional
    public ApiDtos.InterviewResponse update(UUID id, ApiDtos.InterviewRequest request) {
        InterviewEntity interview = getOwnedInterview(id);
        interview.setRoundName(request.roundName());
        interview.setScheduledAt(request.scheduledAt());
        interview.setLocation(request.location());
        interview.setInterviewType(request.interviewType());
        interview.setFeedback(request.feedback());
        interview.setNotes(request.notes());
        if (request.status() != null) {
            interview.setStatus(request.status());
        }
        InterviewEntity saved = interviewRepository.save(interview);
        notificationService.scheduleInterviewReminder(saved, saved.getApplication());
        return toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        interviewRepository.delete(getOwnedInterview(id));
    }

    public ApiDtos.InterviewResponse getById(UUID id) {
        return toResponse(getOwnedInterview(id));
    }

    public List<ApiDtos.InterviewResponse> upcoming() {
        UserEntity user = currentUserService.currentUser();
        return interviewRepository.findTop10ByApplicationUserOrderByScheduledAtAsc(user).stream().map(this::toResponse).toList();
    }

    private InterviewEntity getOwnedInterview(UUID id) {
        UserEntity user = currentUserService.currentUser();
        InterviewEntity interview = interviewRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "INTERVIEW_NOT_FOUND", "Interview not found"));
        if (!interview.getApplication().getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You cannot modify this interview");
        }
        return interview;
    }

    private JobApplicationEntity getOwnedApplication(UUID id) {
        UserEntity user = currentUserService.currentUser();
        JobApplicationEntity application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "APPLICATION_NOT_FOUND", "Application not found"));
        if (!application.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You cannot access this application");
        }
        return application;
    }

    private ApiDtos.InterviewResponse toResponse(InterviewEntity entity) {
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
}
