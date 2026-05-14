package com.applyflow.ai.entity;

import com.applyflow.ai.common.DomainEnums;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "resumes", indexes = {
        @Index(name = "idx_resumes_user", columnList = "user_id"),
        @Index(name = "idx_resumes_title", columnList = "title")
})
public class ResumeEntity extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private JobApplicationEntity application;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "content_type", length = 120)
    private String contentType;

    @Column(name = "storage_path", length = 500)
    private String storagePath;

    @Column(name = "file_data", columnDefinition = "bytea")
    private byte[] fileData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DomainEnums.ResumeSource source;

    @Column(name = "job_description", columnDefinition = "text")
    private String jobDescription;

    @Lob
    @Column(name = "resume_text", columnDefinition = "text")
    private String resumeText;

    @Column(name = "ats_score")
    private Integer atsScore;

    @Column(name = "matched_keywords_json", columnDefinition = "text")
    private String matchedKeywordsJson;

    @Column(name = "missing_keywords_json", columnDefinition = "text")
    private String missingKeywordsJson;

    @Column(name = "suggestions_json", columnDefinition = "text")
    private String suggestionsJson;
}
