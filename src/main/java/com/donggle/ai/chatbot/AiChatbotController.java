package com.donggle.ai.chatbot;

import com.donggle.ai.dto.ChatMemoryResponse;
import com.donggle.ai.dto.ChatRequest;
import com.donggle.ai.dto.ChatResponse;
import com.donggle.ai.dto.HealthResponse;
import com.donggle.ai.service.AiChatbotService;
import com.donggle.global.auth.resolver.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
public class AiChatbotController implements AiChatbotApi {

    private final AiChatbotService aiChatbotService;

    @Override
    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request, @UserId Long userId) {
        ChatResponse response =
                aiChatbotService.chat(
                        request.getMessage(), request.getChatId(), request.getIsNewChat(), userId);
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{chatId}/memory")
    public ResponseEntity<ChatMemoryResponse> clearChatMemory(
            @PathVariable String chatId, @UserId Long userId) {
        aiChatbotService.clearChatMemory(chatId);
        return ResponseEntity.ok(new ChatMemoryResponse("채팅 메모리가 초기화되었습니다."));
    }

    @Override
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> healthCheck() {
        return ResponseEntity.ok(new HealthResponse("AI 챗봇 서비스가 정상 작동 중입니다."));
    }
}
