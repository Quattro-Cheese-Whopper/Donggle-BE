package com.donggle.domain.announce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnnounceRequest {

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    @Size(min = 10, message = "내용은 10자 이상이어야 합니다.")
    private String content;

    private boolean pinned;
}
