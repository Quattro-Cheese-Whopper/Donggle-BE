package com.donggle.ai.service;

import com.donggle.ai.dto.ChatResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatbotService {

    private final ChatClient chatClient;
    private final InMemoryChatMemory chatMemory = new InMemoryChatMemory();

    public ChatResponse chat(String message, String chatId, Boolean isNewChat, Long userId) {
        long startTime = System.currentTimeMillis();

        // ThreadLocal에 사용자 ID 설정
        UserContextHolder.setUserId(userId);

        try {
            // 채팅 세션 ID 결정
            String conversationId = determineConversationId(chatId, isNewChat);

            log.info(
                    "채팅 요청 - userId: {}, chatId: {}, isNewChat: {}, conversationId: {}",
                    userId,
                    chatId,
                    isNewChat,
                    conversationId);

            String systemPrompt = buildSystemPrompt(userId);

            String response =
                    chatClient
                            .prompt()
                            .system(systemPrompt)
                            .user(message)
                            .advisors(new MessageChatMemoryAdvisor(chatMemory, conversationId, 10))
                            .call()
                            .content();

            long processingTime = System.currentTimeMillis() - startTime;

            return ChatResponse.of(response, conversationId, getUsedTools(), processingTime);

        } catch (Exception e) {
            log.error("AI 챗봇 처리 중 오류 발생: {}", e.getMessage(), e);
            String errorChatId = chatId != null ? chatId : generateNewChatId();
            return ChatResponse.simple("죄송합니다. 요청을 처리하는 중 오류가 발생했습니다. 다시 시도해주세요.", errorChatId);
        } finally {
            // ThreadLocal 정리
            UserContextHolder.clear();
        }
    }

    /** 대화 ID 결정 로직 1. 새 채팅이면 새 ID 생성 2. 기존 chatId가 있으면 사용 3. 둘 다 없으면 새 ID 생성 */
    private String determineConversationId(String chatId, Boolean isNewChat) {
        if (isNewChat != null && isNewChat) {
            // 새 채팅 명시적 요청
            String newChatId = generateNewChatId();
            log.info("새 채팅 세션 생성: {}", newChatId);
            return newChatId;
        }

        if (chatId != null && !chatId.isBlank()) {
            // 기존 채팅 ID 사용
            log.info("기존 채팅 세션 사용: {}", chatId);
            return chatId;
        }

        // 채팅 ID가 없으면 새로 생성
        String newChatId = generateNewChatId();
        log.info("채팅 ID 없음, 새 세션 생성: {}", newChatId);
        return newChatId;
    }

    /** 새로운 채팅 세션 ID 생성 */
    private String generateNewChatId() {
        return "chat-" + UUID.randomUUID();
    }

    /** 채팅 세션 메모리 초기화 (새 채팅용) */
    public void clearChatMemory(String chatId) {
        if (chatId != null) {
            chatMemory.clear(chatId);
            log.info("채팅 메모리 초기화: {}", chatId);
        }
    }

    private String buildSystemPrompt(Long userId) {
        return """
                너는 전남대학교 동아리 통합 모집 플랫폼 '동글(Donggle)'의 AI 어시스턴트야.
                사용자의 동아리 관련 질문에 친근하고 도움이 되는 답변을 제공해줘.

                **주요 기능:**

                1. **동아리 검색 및 추천** (searchClubs):
                   - "컴퓨터 관련 동아리 찾아줘" → keyword="컴퓨터"
                   - "체육 동아리 추천해줘" → category="체육분과"
                   - "중앙동아리 중에서 학술 관련" → type="CENTRAL", category="학술분과"

                2. **모집 공고 조회** (getRecruitments):
                   - "현재 모집 중인 동아리 알려줘" → action="active"
                   - "마감된 모집 공고 보여줘" → action="byStatus", status="COMPLETED"

                3. **내 지원 현황** (getMyApplications):
                   - "내가 지원한 동아리 상태 알려줘" → 전체 조회
                   - "합격한 동아리 있어?" → status="APPROVED"

                4. **알림 관리**:
                   - "새로운 알림 있어?" → getUnreadNotifications
                   - "알림 요약해줘" → getNotificationSummary
                   - "최근 알림 보여줘" → getRecentNotifications

                **응답 가이드라인:**
                - 검색 결과를 받으면 사용자에게 유용한 인사이트 제공
                - 구체적인 정보와 함께 다음 액션 제안
                - 이모지 사용해서 읽기 쉽게 구성
                - 사용자 맞춤형 조언 제공
                - 검색 결과가 없으면 대안 제안

                현재 사용자 ID: """
                + userId
                + """

                함수 호출 결과를 바탕으로 개인화된 조언과 다음 단계를 제안해줘.
                """;
    }

    private java.util.List<String> getUsedTools() {
        // 실제로는 함수 호출 이력을 추적해야 하지만, 일단 빈 리스트 반환
        return java.util.List.of();
    }
}
