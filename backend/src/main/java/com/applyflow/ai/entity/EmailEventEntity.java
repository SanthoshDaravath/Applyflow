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
import java.time.Instant;
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
@Table(name = "email_events", indexes = {
        @Index(name = "idx_email_events_message_id", columnList = "message_id", unique = true),
        @Index(name = "idx_email_events_user", columnList = "user_id"),
        @Index(name = "idx_email_events_classification", columnList = "classification")
})
public class EmailEventEntity extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private JobApplicationEntity application;

    @Column(name = "message_id", nullable = false, unique = true, length = 255)
    private String messageId;

    @Column(name = "thread_id", length = 255)
    private String threadId;

    @Column(name = "from_address", length = 255)
    private String fromAddress;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(columnDefinition = "text")
    private String body;

    @Column(name = "body_snippet", columnDefinition = "text")
    private String bodySnippet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DomainEnums.EmailClassification classification;

    @Column(nullable = false)
    private double confidence;

    @Column(name = "metadata_json", columnDefinition = "text")
    private String metadataJson;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;
}
