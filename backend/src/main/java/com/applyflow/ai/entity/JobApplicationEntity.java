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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
@Table(name = "applications", indexes = {
        @Index(name = "idx_applications_user_status", columnList = "user_id,status"),
        @Index(name = "idx_applications_company", columnList = "company"),
        @Index(name = "idx_applications_source", columnList = "source_platform")
})
public class JobApplicationEntity extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, length = 180)
    private String company;

    @Column(name = "role_name", nullable = false, length = 180)
    private String roleName;

    @Column(precision = 12, scale = 2)
    private BigDecimal salary;

    @Column(length = 180)
    private String location;

    @Column(name = "source_platform", nullable = false, length = 80)
    private String sourcePlatform;

    @Column(name = "application_date", nullable = false)
    private LocalDate applicationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DomainEnums.ApplicationStatus status;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "job_url", columnDefinition = "text")
    private String jobUrl;

    @Column(name = "resume_snapshot", columnDefinition = "text")
    private String resumeSnapshot;

    @OneToMany(mappedBy = "application")
    @Builder.Default
    private List<InterviewEntity> interviews = new ArrayList<>();
}
