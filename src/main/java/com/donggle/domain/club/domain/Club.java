package com.donggle.domain.club.domain;

import com.donggle.domain.user.domain.User;
import com.donggle.global.persistence.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clubs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Club extends BaseEntity {

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

    private String profileImageName;

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
        // 중앙동아리 분과
        학술분과,
        문예분과,
        체육분과,
        봉사분과,
        종교분과,
        기타,

        // 단과대학
        농업생명과학대학,
        사회과학대학,
        수의과대학,
        치의학전문대학원,
        경영대학,
        인문대학,
        AI융합대학,
        본부직할,
        사범대학,
        예술대학,
        공과대학,
        간호대학,
        의과대학,
        생활과학대학,
        약학대학,
        자연과학대학
    }

    public Club(
            String name,
            ClubType type,
            ClubCategory category,
            String description,
            Integer memberCount,
            String location,
            String contactInfo,
            String profileImageName) {
        this.name = name;
        this.type = type;
        this.category = category;
        this.description = description;
        this.memberCount = memberCount;
        this.location = location;
        this.contactInfo = contactInfo;
        this.profileImageName = profileImageName;
    }

    public void update(
            String name,
            ClubType type,
            ClubCategory category,
            String description,
            Integer memberCount,
            String location,
            String contactInfo,
            String profileImageName) {
        this.name = name;
        this.type = type;
        this.category = category;
        this.description = description;
        this.memberCount = memberCount;
        this.location = location;
        this.contactInfo = contactInfo;
        this.profileImageName = profileImageName;
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
