package com.applyflow.ai.service;

import com.applyflow.ai.common.DomainEnums;
import com.applyflow.ai.dto.ApiDtos;
import com.applyflow.ai.entity.InterviewEntity;
import com.applyflow.ai.entity.JobApplicationEntity;
import com.applyflow.ai.entity.NotificationEntity;
import com.applyflow.ai.entity.UserEntity;
import com.applyflow.ai.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final CurrentUserService currentUserService;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public NotificationService(NotificationRepository notificationRepository, CurrentUserService currentUserService, ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.notificationRepository = notificationRepository;
        this.currentUserService = currentUserService;
        this.mailSenderProvider = mailSenderProvider;
    }

    @Transactional
    public ApiDtos.NotificationResponse create(ApiDtos.NotificationRequest request) {
        UserEntity user = currentUserService.currentUser();
        NotificationEntity notification = notificationRepository.save(NotificationEntity.builder()
                .user(user)
                .channel(request.channel() != null ? request.channel() : DomainEnums.NotificationChannel.IN_APP)
                .title(request.title())
                .message(request.message())
                .status(DomainEnums.NotificationStatus.PENDING)
                .scheduledAt(request.scheduledAt())
                .relatedEntityId(request.relatedApplicationId() != null ? request.relatedApplicationId().toString() : request.relatedInterviewId() != null ? request.relatedInterviewId().toString() : null)
                .relatedEntityType(request.relatedApplicationId() != null ? "APPLICATION" : request.relatedInterviewId() != null ? "INTERVIEW" : null)
                .build());
        return toResponse(notification);
    }

    @Transactional
    public ApiDtos.NotificationResponse scheduleInterviewReminder(InterviewEntity interview, JobApplicationEntity application) {
        LocalDateTime reminderTime = interview.getScheduledAt().minusHours(24);
        if (reminderTime.isBefore(LocalDateTime.now())) {
            reminderTime = LocalDateTime.now().plusMinutes(5);
        }
        NotificationEntity notification = notificationRepository.save(NotificationEntity.builder()
                .user(application.getUser())
                .application(application)
                .interview(interview)
                .channel(DomainEnums.NotificationChannel.EMAIL)
                .title("Interview reminder: " + application.getCompany())
                .message("Upcoming " + interview.getRoundName() + " for " + application.getRoleName() + " at " + application.getCompany())
                .status(DomainEnums.NotificationStatus.PENDING)
                .scheduledAt(reminderTime)
                .relatedEntityType("INTERVIEW")
                .relatedEntityId(interview.getId().toString())
                .build());
        return toResponse(notification);
    }

    public List<ApiDtos.NotificationResponse> listMine() {
        return notificationRepository.findTop20ByUserOrderByCreatedAtDesc(currentUserService.currentUser())
                .stream().map(this::toResponse).toList();
    }

    @Scheduled(fixedDelayString = "${app.notifications.tick-ms:60000}")
    @Transactional
    public void processDueNotifications() {
        LocalDateTime now = LocalDateTime.now();
        for (NotificationEntity notification : notificationRepository.findAll()) {
            if (notification.getStatus() == DomainEnums.NotificationStatus.PENDING && notification.getScheduledAt() != null && !notification.getScheduledAt().isAfter(now)) {
                dispatch(notification);
            }
        }
    }

    @Transactional
    public void markSent(UUID notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId).orElseThrow();
        dispatch(notification);
    }

    private void dispatch(NotificationEntity notification) {
        if (notification.getStatus() != DomainEnums.NotificationStatus.PENDING) {
            return;
        }
        try {
            JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
            if (mailSender != null && notification.getUser().getEmail() != null) {
                log.info("Sending notification to {}: {}", notification.getUser().getEmail(), notification.getTitle());
            }
            notification.setStatus(DomainEnums.NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
        } catch (Exception exception) {
            notification.setStatus(DomainEnums.NotificationStatus.FAILED);
            notificationRepository.save(notification);
            log.warn("Failed to send notification {}", notification.getId(), exception);
        }
    }

    private ApiDtos.NotificationResponse toResponse(NotificationEntity entity) {
        return new ApiDtos.NotificationResponse(
                entity.getId(),
                entity.getChannel(),
                entity.getTitle(),
                entity.getMessage(),
                entity.getStatus(),
                entity.getScheduledAt(),
                entity.getSentAt(),
                entity.getCreatedAt());
    }
}
