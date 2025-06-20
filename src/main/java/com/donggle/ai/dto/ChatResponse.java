package com.donggle.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "AI 챗봇 응답")
public class ChatResponse {

    @Schema(description = "AI 응답 메시지")
    private String message;

    @Schema(description = "채팅 세션 ID")
    private String chatId;

    @Schema(description = "사용된 도구 목록")
    private List<String> usedTools;

    @Schema(description = "응답 생성 시간")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime responseTime;

    @Schema(description = "응답 처리 시간 (밀리초)")
    private Long processingTimeMs;

    public ChatResponse(
            String message, String chatId, List<String> usedTools, Long processingTimeMs) {
        this.message = message;
        this.chatId = chatId;
        this.usedTools = usedTools;
        this.responseTime = LocalDateTime.now();
        this.processingTimeMs = processingTimeMs;
    }

    public static ChatResponse of(
            String message, String chatId, List<String> usedTools, Long processingTimeMs) {
        return new ChatResponse(message, chatId, usedTools, processingTimeMs);
    }

    public static ChatResponse simple(String message, String chatId) {
        return new ChatResponse(message, chatId, List.of(), 0L);
    }
}
