package com.donggle.domain.recruitment.dto;

import com.donggle.domain.recruitment.domain.Application;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusRequest {

    @NotNull(message = "지원 상태는 필수 입력값입니다.")
    private Application.ApplicationStatus status;
}
