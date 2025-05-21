package com.donggle.domain.recruitment.dto;

import com.donggle.domain.recruitment.domain.Application;
import com.donggle.domain.user.dto.UserProfileResponse;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {

    private Long id;
    private RecruitmentResponse recruitment;
    private UserProfileResponse user;
    private String content;
    private Application.ApplicationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ApplicationResponse from(Application application) {
        return new ApplicationResponse(
                application.getId(),
                RecruitmentResponse.from(application.getRecruitment()),
                UserProfileResponse.from(application.getUser()),
                application.getContent(),
                application.getStatus(),
                application.getCreatedAt(),
                application.getUpdatedAt());
    }

    // 간략한 버전 - 중복 정보를 줄임
    public static ApplicationResponse briefFrom(Application application) {
        return new ApplicationResponse(
                application.getId(),
                null, // RecruitmentResponse 생략
                UserProfileResponse.from(application.getUser()),
                application.getContent(),
                application.getStatus(),
                application.getCreatedAt(),
                application.getUpdatedAt());
    }
}
