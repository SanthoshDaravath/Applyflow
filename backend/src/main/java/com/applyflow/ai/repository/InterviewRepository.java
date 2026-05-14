package com.applyflow.ai.repository;

import com.applyflow.ai.entity.InterviewEntity;
import com.applyflow.ai.entity.UserEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface InterviewRepository extends JpaRepository<InterviewEntity, UUID> {
    @EntityGraph(attributePaths = {"application"})
    List<InterviewEntity> findTop10ByApplicationUserOrderByScheduledAtAsc(UserEntity user);

    @EntityGraph(attributePaths = {"application"})
    List<InterviewEntity> findByScheduledAtBetween(LocalDateTime start, LocalDateTime end);
}
