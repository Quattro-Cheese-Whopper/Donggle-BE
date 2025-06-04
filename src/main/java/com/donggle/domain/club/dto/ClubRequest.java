package com.donggle.domain.club.dto;

import com.donggle.domain.club.domain.Club;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClubRequest {

    @NotBlank(message = "동아리 이름은 필수 입력값입니다.")
    private String name;

    @NotNull(message = "동아리 타입은 필수 입력값입니다.")
    private Club.ClubType type;

    @NotNull(message = "동아리 카테고리는 필수 입력값입니다.")
    private Club.ClubCategory category;

    private String description;

    private Integer memberCount;

    private String location;

    private String contactInfo;

    private String profileImageName;

    // 기본값이 적용된 값을 반환하는 메서드들 (Swagger에서 숨김)
    @Schema(hidden = true)
    public String getDescriptionWithDefault() {
        return description != null && !description.trim().isEmpty()
                ? description
                : "동아리 소개가 준비중입니다.";
    }

    @Schema(hidden = true)
    public Integer getMemberCountWithDefault() {
        return memberCount != null ? memberCount : 1;
    }

    @Schema(hidden = true)
    public String getLocationWithDefault() {
        return location != null && !location.trim().isEmpty() ? location : "위치 정보가 준비중입니다.";
    }

    @Schema(hidden = true)
    public String getContactInfoWithDefault() {
        return contactInfo != null && !contactInfo.trim().isEmpty()
                ? contactInfo
                : "연락처 정보가 준비중입니다.";
    }
}
