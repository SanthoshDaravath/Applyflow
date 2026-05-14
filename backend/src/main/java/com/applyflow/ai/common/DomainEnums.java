package com.applyflow.ai.common;

public final class DomainEnums {

    private DomainEnums() {
    }

    public enum UserRole {
        USER,
        ADMIN
    }

    public enum ApplicationStatus {
        SAVED,
        APPLIED,
        ONLINE_ASSESSMENT,
        INTERVIEW,
        OFFER,
        REJECTED
    }

    public enum EmailClassification {
        APPLIED,
        INTERVIEW,
        REJECTED,
        OFFER,
        ASSESSMENT,
        FOLLOW_UP,
        UNKNOWN
    }

    public enum NotificationChannel {
        EMAIL,
        IN_APP,
        TELEGRAM,
        WHATSAPP
    }

    public enum NotificationStatus {
        PENDING,
        SENT,
        FAILED
    }

    public enum ResumeSource {
        MANUAL,
        UPLOADED,
        LINKED
    }

    public enum InterviewStatus {
        SCHEDULED,
        COMPLETED,
        RESCHEDULED,
        CANCELLED
    }

    public enum InsightType {
        PLATFORM_PERFORMANCE,
        RESUME_GAP,
        RESPONSE_TREND,
        APPLICATION_TIMING,
        SKILL_GAP
    }
}
