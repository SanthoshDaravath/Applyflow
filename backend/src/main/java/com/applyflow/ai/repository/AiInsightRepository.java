package com.applyflow.ai.repository;

import com.applyflow.ai.common.DomainEnums;
import com.applyflow.ai.entity.AiInsightEntity;
import com.applyflow.ai.entity.UserEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiInsightRepository extends JpaRepository<AiInsightEntity, UUID> {
    List<AiInsightEntity> findTop10ByUserOrderByGeneratedAtDesc(UserEntity user);
    List<AiInsightEntity> findByUserAndInsightTypeOrderByGeneratedAtDesc(UserEntity user, DomainEnums.InsightType type);
}
