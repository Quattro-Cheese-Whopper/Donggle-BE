package com.donggle.domain.user.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String studentId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public enum UserRole {
        GUEST,
        MANAGER,
        ADMIN
    }

    public User(String email, String password, String studentId, String name) {
        this.email = email;
        this.password = password;
        this.studentId = studentId;
        this.name = name;
        this.role = UserRole.GUEST;
        this.createdAt = LocalDateTime.now();
    }

    public void updateRole(UserRole role) {
        this.role = role;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePassword(String password) {
        this.password = password;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }
}
