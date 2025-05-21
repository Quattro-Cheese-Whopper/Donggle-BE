package com.donggle.domain.announce.domain;

import com.donggle.domain.club.domain.Club;
import com.donggle.domain.user.domain.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "announces")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Announce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnnounceType type;

    @Column(nullable = false)
    private boolean pinned;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum AnnounceType {
        GENERAL, // 일반 공지사항
        CLUB // 동아리 공지사항
    }

    public Announce(User author, Club club, String title, String content, boolean pinned) {
        this.author = author;
        this.club = club;
        this.title = title;
        this.content = content;
        this.type = club == null ? AnnounceType.GENERAL : AnnounceType.CLUB;
        this.pinned = pinned;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void update(String title, String content, boolean pinned) {
        this.title = title;
        this.content = content;
        this.pinned = pinned;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAuthor(User user) {
        return this.author.getId().equals(user.getId());
    }

    public boolean isClubManager(User user) {
        return this.club != null && this.club.isManager(user);
    }
}
