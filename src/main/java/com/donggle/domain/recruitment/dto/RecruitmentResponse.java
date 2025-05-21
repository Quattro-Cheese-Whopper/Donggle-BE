package com.donggle.domain.recruitment.dto;

import com.donggle.domain.club.dto.ClubResponse;
import com.donggle.domain.recruitment.domain.Recruitment;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentResponse {

    private Long id;
    private ClubResponse club;
    private String title;
    private String content;
    private Integer recruitCount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Recruitment.RecruitmentStatus status;
    private String contactInfo;
    private String applicationLink;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RecruitmentResponse from(Recruitment recruitment) {
        return new RecruitmentResponse(
                recruitment.getId(),
                ClubResponse.from(recruitment.getClub()),
                recruitment.getTitle(),
                recruitment.getContent(),
                recruitment.getRecruitCount(),
                recruitment.getStartDate(),
                recruitment.getEndDate(),
                recruitment.getStatus(),
                recruitment.getContactInfo(),
                recruitment.getApplicationLink(),
                recruitment.getCreatedAt(),
                recruitment.getUpdatedAt());
    }
}
