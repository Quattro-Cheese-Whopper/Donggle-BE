package com.donggle.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "AI 챗봇 요청")
public class ChatRequest {

    @NotBlank(message = "메시지는 필수입니다.")
    @Schema(description = "사용자 메시지", example = "컴퓨터 관련 동아리 추천해줘")
    private String message;

    public ChatRequest(String message) {
        this.message = message;
    }
}
