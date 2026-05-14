package com.applyflow.ai.repository;

import com.applyflow.ai.common.DomainEnums;
import com.applyflow.ai.entity.JobApplicationEntity;
import com.applyflow.ai.entity.UserEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<JobApplicationEntity, UUID> {
    Page<JobApplicationEntity> findByUser(UserEntity user, Pageable pageable);
    Page<JobApplicationEntity> findByUserAndStatus(UserEntity user, DomainEnums.ApplicationStatus status, Pageable pageable);
    List<JobApplicationEntity> findByUserOrderByCreatedAtDesc(UserEntity user);
    long countByUser(UserEntity user);
    long countByUserAndStatus(UserEntity user, DomainEnums.ApplicationStatus status);
}
