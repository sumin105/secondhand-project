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
    @Column(length = 30, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // 기본값 설정 필요

    @Setter
    @Column(nullable = false)
    private boolean deleted = false;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(length = 255)
    private String email;

    @Setter
    @Column(length = 11)
    private String phoneNumber;

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
    @Column(length = 500)
    private String intro;

    private int reportCount;


    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 계정 생성 시간

    @Column(nullable = false)
    private LocalDateTime updatedAt; // 마지막 수정 시간

    public User(String oauthId, String provider, String name, String email) {
        this.oauthId = oauthId;
        this.provider = provider;
        this.name = name;
        this.email = email;
        this.role = Role.USER;
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

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.role == null) {
            this.role = Role.USER; // 기본값 User 설정
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
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
