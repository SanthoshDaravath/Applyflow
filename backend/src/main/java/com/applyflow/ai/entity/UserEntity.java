package com.applyflow.ai.entity;

import com.applyflow.ai.common.DomainEnums;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_role", columnList = "role")
})
public class UserEntity extends AuditableEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 180)
    private String fullName;

    @Column(length = 40)
    private String provider;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DomainEnums.UserRole role;

    @Column(nullable = false)
    @ColumnDefault("true")
    private boolean enabled = true;

    @Column(name = "email_verified", nullable = false)
    @ColumnDefault("false")
    private boolean emailVerified = false;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private Set<RefreshTokenEntity> refreshTokens = new HashSet<>();
}
