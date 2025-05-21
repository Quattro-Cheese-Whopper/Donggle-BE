package com.donggle.domain.recruitment.dto;

import com.donggle.domain.recruitment.domain.Recruitment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentRequest {

    @NotBlank(message = "제목은 필수 입력값입니다.")
    private String title;

    private String content;

    private Integer recruitCount;

    @NotNull(message = "모집 시작일은 필수 입력값입니다.")
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @NotNull(message = "모집 상태는 필수 입력값입니다.")
    private Recruitment.RecruitmentStatus status;

    private String contactInfo;

    private String applicationLink;
}
