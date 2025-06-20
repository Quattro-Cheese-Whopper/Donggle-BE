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

    @Schema(
            description = "채팅 세션 ID (없으면 새 세션 생성)",
            example = "chat-550e8400-e29b-41d4-a716-446655440000")
    private String chatId;

    @Schema(description = "새 채팅 시작 여부", example = "false")
    private Boolean isNewChat = false;

    public ChatRequest(String message) {
        this.message = message;
    }

    public ChatRequest(String message, String chatId, Boolean isNewChat) {
        this.message = message;
        this.chatId = chatId;
        this.isNewChat = isNewChat != null ? isNewChat : false;
    }
}
