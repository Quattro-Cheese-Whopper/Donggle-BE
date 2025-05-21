package com.donggle.domain.recruitment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequest {

    @NotBlank(message = "지원서 내용은 필수 입력값입니다.")
    private String content;
}
