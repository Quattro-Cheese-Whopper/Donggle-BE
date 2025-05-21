package com.donggle.domain.club.dto;

import com.donggle.domain.club.domain.Club;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClubResponse {

    private Long id;
    private String name;
    private Club.ClubType type;
    private Club.ClubCategory category;
    private String description;
    private Integer memberCount;
    private String location;
    private String contactInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ClubResponse from(Club club) {
        return new ClubResponse(
                club.getId(),
                club.getName(),
                club.getType(),
                club.getCategory(),
                club.getDescription(),
                club.getMemberCount(),
                club.getLocation(),
                club.getContactInfo(),
                club.getCreatedAt(),
                club.getUpdatedAt());
    }
}
