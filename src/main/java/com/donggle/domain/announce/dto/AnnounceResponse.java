package com.donggle.domain.announce.dto;

import com.donggle.domain.announce.domain.Announce;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnnounceResponse {

    private Long id;
    private Long authorId;
    private String authorName;
    private Long clubId;
    private String clubName;
    private String title;
    private String content;
    private Announce.AnnounceType type;
    private boolean pinned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AnnounceResponse from(Announce announce) {
        return new AnnounceResponse(
                announce.getId(),
                announce.getAuthor().getId(),
                announce.getAuthor().getName(),
                announce.getClub() != null ? announce.getClub().getId() : null,
                announce.getClub() != null ? announce.getClub().getName() : null,
                announce.getTitle(),
                announce.getContent(),
                announce.getType(),
                announce.isPinned(),
                announce.getCreatedAt(),
                announce.getUpdatedAt());
    }
}
