package com.donggle.domain.user.domain;

import com.donggle.global.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends BaseEntity {

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
    }

    public void updateRole(UserRole role) {
        this.role = role;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }
}
