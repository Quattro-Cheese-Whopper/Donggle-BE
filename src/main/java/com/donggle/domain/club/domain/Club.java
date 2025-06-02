package com.donggle.domain.club.domain;

import com.donggle.domain.user.domain.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clubs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClubType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClubCategory category;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer memberCount;

    private String location;

    private String contactInfo;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "club_managers",
            joinColumns = @JoinColumn(name = "club_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private final List<User> managers = new ArrayList<>();

    public enum ClubType {
        CENTRAL, // 중앙 동아리
        DEPARTMENT // 학과 동아리
    }

    public enum ClubCategory {
        ACADEMIC, // 학술
        CULTURE, // 문화
        SPORTS, // 체육
        VOLUNTEER, // 봉사
        RELIGION, // 종교
        OTHER // 기타
    }

    public Club(
            String name,
            ClubType type,
            ClubCategory category,
            String description,
            Integer memberCount,
            String location,
            String contactInfo) {
        this.name = name;
        this.type = type;
        this.category = category;
        this.description = description;
        this.memberCount = memberCount;
        this.location = location;
        this.contactInfo = contactInfo;
    }

    public void update(
            String name,
            ClubType type,
            ClubCategory category,
            String description,
            Integer memberCount,
            String location,
            String contactInfo) {
        this.name = name;
        this.type = type;
        this.category = category;
        this.description = description;
        this.memberCount = memberCount;
        this.location = location;
        this.contactInfo = contactInfo;
        this.updatedAt = LocalDateTime.now();
    }

    public void addManager(User user) {
        if (!this.managers.contains(user)) {
            this.managers.add(user);
        }
    }

    public void removeManager(User user) {
        this.managers.remove(user);
    }

    public boolean isManager(User user) {
        return this.managers.stream().anyMatch(manager -> manager.getId().equals(user.getId()));
    }
}
