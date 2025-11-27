package com.example.demo.entities;

import com.example.demo.commons.enums.AccountRole;
import com.example.demo.commons.enums.AccountStatus;
import com.example.demo.commons.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)")
    private AuthProvider provider;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "SMALLINT")
    private AccountRole role;

    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "SMALLINT")
    private AccountStatus status;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private User user;
}
