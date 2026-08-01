package com.applyflow.ai.dto;

import com.applyflow.ai.common.DomainEnums;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class ApiDtos {

    private ApiDtos() {
    }

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            UserResponse user,
            String tokenType,
            Instant expiresAt) {
    }

    public record OAuthExchangeResponse(
            String accessToken,
            String refreshToken,
            UserResponse user) {
    }

    public record UserResponse(
            UUID id,
            String email,
            String fullName,
            DomainEnums.UserRole role,
            boolean emailVerified) {
    }

    public record RegisterRequest(@NotBlank String fullName, @NotBlank String email, @NotBlank String password) {
    }

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record ForgotPasswordRequest(@NotBlank String email) {
    }

    public record ResetPasswordRequest(@NotBlank String token, @NotBlank String newPassword) {
    }

    public record ApplicationRequest(
            @NotBlank String company,
            @NotBlank String role,
            BigDecimal salary,
            String location,
            @NotBlank String sourcePlatform,
            LocalDate applicationDate,
            DomainEnums.ApplicationStatus status,
            String notes,
            String jobUrl,
            String resumeSnapshot) {
    }

    public record ApplicationResponse(
            UUID id,
            String company,
            String role,
            BigDecimal salary,
            String location,
            String sourcePlatform,
            LocalDate applicationDate,
            DomainEnums.ApplicationStatus status,
            String notes,
            String jobUrl,
            String resumeSnapshot,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record StatusUpdateRequest(DomainEnums.ApplicationStatus status, String notes) {
    }

    public record ApplicationStatsResponse(
            long totalApplications,
            long savedCount,
            long appliedCount,
            long onlineAssessmentCount,
            long interviewCount,
            long offerCount,
            long rejectedCount,
            double interviewRate,
            double offerRate,
            double rejectionRate,
            Map<String, Long> applicationsByPlatform) {
    }

    public record KanbanColumnResponse(String status, List<ApplicationResponse> items) {
    }

    public record ResumeUploadRequest(String title, @NotBlank String jobDescription, @NotBlank String resumeText) {
    }

    public record ResumeResponse(
            UUID id,
            String title,
            String fileName,
            String jobDescription,
            String resumeText,
            Integer atsScore,
            List<String> matchedKeywords,
            List<String> missingKeywords,
            List<String> suggestions,
            DomainEnums.ResumeSource source,
            Instant createdAt) {
    }

    public record ResumeAnalysisResponse(
            int atsScore,
            List<String> matchedKeywords,
            List<String> missingKeywords,
            List<String> suggestions,
            String summary) {
    }

    public record InterviewRequest(
            @NotNull UUID applicationId,
            @NotBlank String roundName,
            @NotNull LocalDateTime scheduledAt,
            String location,
            String interviewType,
            String feedback,
            String notes,
            DomainEnums.InterviewStatus status) {
    }

    public record InterviewResponse(
            UUID id,
            UUID applicationId,
            String company,
            String role,
            String roundName,
            LocalDateTime scheduledAt,
            String location,
            String interviewType,
            String feedback,
            String notes,
            DomainEnums.InterviewStatus status,
            boolean reminderSent,
            Instant createdAt) {
    }

    public record EmailEventRequest(
            UUID userId,
            @NotBlank String messageId,
            String fromAddress,
            @NotBlank String subject,
            @NotBlank String body,
            String threadId,
            String sourceProvider) {
    }

    public record EmailEventResponse(
            UUID id,
            String messageId,
            String fromAddress,
            String subject,
            String bodySnippet,
            DomainEnums.EmailClassification classification,
            double confidence,
            Map<String, Object> metadata,
            Instant receivedAt,
            Instant processedAt) {
    }

    public record NotificationRequest(
            DomainEnums.NotificationChannel channel,
            @NotBlank String title,
            @NotBlank String message,
            LocalDateTime scheduledAt,
            UUID relatedApplicationId,
            UUID relatedInterviewId) {
    }

    public record NotificationResponse(
            UUID id,
            DomainEnums.NotificationChannel channel,
            String title,
            String message,
            DomainEnums.NotificationStatus status,
            LocalDateTime scheduledAt,
            LocalDateTime sentAt,
            Instant createdAt) {
    }

    public record InsightResponse(
            UUID id,
            DomainEnums.InsightType type,
            String title,
            String summary,
            List<String> recommendations,
            double confidence,
            Instant generatedAt) {
    }

    public record DashboardResponse(
            ApplicationStatsResponse applicationStats,
            List<TimePoint> applicationTimeline,
            List<CategoryPoint> platformBreakdown,
            List<InsightResponse> insights,
            List<InterviewResponse> upcomingInterviews) {
    }

    public record TimePoint(String label, long value) {
    }

    public record CategoryPoint(String label, long value) {
    }

    public record PageResponse<T>(List<T> items, int page, int size, long totalElements, int totalPages) {
        public static <T> PageResponse<T> from(Page<T> page) {
            return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
        }
    }

    public record OAuthCodeExchangeRequest(@NotBlank String code) {
    }

    public record ForgotPasswordResponse(String message) {
    }

    public record GenericMessageResponse(String message) {
    }

    public record HealthResponse(String status, String application, Instant timestamp) {
    }
}
