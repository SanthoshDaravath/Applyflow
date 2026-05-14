package com.applyflow.ai.repository;

import com.applyflow.ai.entity.RefreshTokenEntity;
import com.applyflow.ai.entity.UserEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
    List<RefreshTokenEntity> findAllByUserAndRevokedFalseAndExpiresAtAfter(UserEntity user, Instant now);
    void deleteByUser(UserEntity user);
}
