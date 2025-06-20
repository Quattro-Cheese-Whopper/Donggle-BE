package com.donggle.ai.chatbot;

import com.donggle.ai.dto.ChatRequest;
import com.donggle.ai.dto.ChatResponse;
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
        ChatResponse response = aiChatbotService.chat(request.getMessage(), userId);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("AI 챗봇 서비스가 정상 동작 중입니다.");
    }
}
