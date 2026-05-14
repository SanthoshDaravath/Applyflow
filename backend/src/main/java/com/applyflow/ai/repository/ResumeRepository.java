package com.applyflow.ai.repository;

import com.applyflow.ai.entity.ResumeEntity;
import com.applyflow.ai.entity.UserEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<ResumeEntity, UUID> {
    List<ResumeEntity> findByUserOrderByCreatedAtDesc(UserEntity user);
    List<ResumeEntity> findTop5ByUserOrderByAtsScoreDescCreatedAtDesc(UserEntity user);
}
