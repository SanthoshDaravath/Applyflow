package com.applyflow.ai.repository;

import com.applyflow.ai.common.DomainEnums;
import com.applyflow.ai.entity.EmailEventEntity;
import com.applyflow.ai.entity.UserEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailEventRepository extends JpaRepository<EmailEventEntity, UUID> {
    boolean existsByMessageId(String messageId);
    java.util.Optional<EmailEventEntity> findByMessageId(String messageId);
    List<EmailEventEntity> findTop20ByUserOrderByReceivedAtDesc(UserEntity user);
    long countByUserAndClassification(UserEntity user, DomainEnums.EmailClassification classification);
}
