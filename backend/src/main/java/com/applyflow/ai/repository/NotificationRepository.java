package com.applyflow.ai.repository;

import com.applyflow.ai.common.DomainEnums;
import com.applyflow.ai.entity.NotificationEntity;
import com.applyflow.ai.entity.UserEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    List<NotificationEntity> findTop20ByUserOrderByCreatedAtDesc(UserEntity user);
    List<NotificationEntity> findByUserAndStatusAndScheduledAtBefore(UserEntity user, DomainEnums.NotificationStatus status, LocalDateTime time);
}
