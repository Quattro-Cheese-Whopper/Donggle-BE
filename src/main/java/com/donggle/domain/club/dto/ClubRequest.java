package com.donggle.domain.club.dto;

import com.donggle.domain.club.domain.Club;
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

    @NotBlank(message = "동아리 설명은 필수 입력값입니다.")
    private String description;

    @NotNull(message = "동아리 인원 수는 필수 입력값입니다.")
    private Integer memberCount;

    @NotBlank(message = "동아리 위치는 필수 입력값입니다.")
    private String location;

    @NotBlank(message = "동아리 연락처는 필수 입력값입니다.")
    private String contactInfo;
}
