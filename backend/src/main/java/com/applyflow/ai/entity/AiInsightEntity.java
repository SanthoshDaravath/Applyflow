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
@Table(name = "ai_insights", indexes = {
        @Index(name = "idx_ai_insights_user", columnList = "user_id"),
        @Index(name = "idx_ai_insights_type", columnList = "insight_type")
})
public class AiInsightEntity extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private JobApplicationEntity application;

    @Enumerated(EnumType.STRING)
    @Column(name = "insight_type", nullable = false, length = 60)
    private DomainEnums.InsightType insightType;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "text")
    private String summary;

    @Column(name = "recommendations_json", columnDefinition = "text")
    private String recommendationsJson;

    @Column(nullable = false)
    private double confidence;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;
}
