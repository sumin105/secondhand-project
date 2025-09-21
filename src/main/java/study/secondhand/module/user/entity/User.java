package study.secondhand.module.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String oauthId; // 소셜 로그인 ID

    @Column(nullable = false, length = 50)
    private String provider; // 소셜 로그인 제공자

    @Setter
    @Column(length = 500)
    private String refreshToken;

    @Column(nullable = false, length = 255)
    private String name;

    @Setter
    @Column(length = 12, unique = true)
    private String nickname;

    @Column(length = 255)
    private String email;

    @Setter
    @Column(length = 15)
    private String phoneNumber;

    @Setter
    @Column(length = 500)
    private String intro;

    @Setter
    @Column(length = 255)
    private String address;

    @Setter
    @Column(length = 255)
    private String detailAddress;

    @Setter
    @Column(length = 5)
    private String postCode;

    @Setter
    @Column(length = 30)
    private String storeName;

    @Setter
    @Column(length = 255)
    private String storeAddress;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Setter
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    private int reportCount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public User(String oauthId, String provider, String name, String email) {
        this.oauthId = oauthId;
        this.provider = provider;
        this.name = name;
        this.email = email;
        this.role = Role.USER;
        this.status = UserStatus.ACTIVE;
        this.deleted = false;
    }

    public void increaseReportCount() {
        this.reportCount++;
    }

    public enum Role {
        USER, ADMIN, SYSTEM
    }

    public enum UserStatus {
        ACTIVE, // 정상 회원
        WITHDRAWN, // 회원 탈퇴
        BANNED // 정지된 회원
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public boolean isSystem() {
        return this.role == Role.SYSTEM;
    }

    public boolean isBanned() {
        return this.status == UserStatus.BANNED;
    }

    public boolean isWithdrawn() {
        return this.status == UserStatus.WITHDRAWN;
    }
}
