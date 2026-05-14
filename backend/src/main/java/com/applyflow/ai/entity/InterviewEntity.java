package com.applyflow.ai.entity;

import com.applyflow.ai.common.DomainEnums;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "interviews", indexes = {
        @Index(name = "idx_interviews_application", columnList = "application_id"),
        @Index(name = "idx_interviews_scheduled", columnList = "scheduled_at")
})
public class InterviewEntity extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private JobApplicationEntity application;

    @Column(name = "round_name", nullable = false, length = 120)
    private String roundName;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(length = 180)
    private String location;

    @Column(name = "interview_type", length = 120)
    private String interviewType;

    @Column(columnDefinition = "text")
    private String feedback;

    @Column(columnDefinition = "text")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DomainEnums.InterviewStatus status;

    @Column(name = "reminder_sent", nullable = false)
    private boolean reminderSent;
}
