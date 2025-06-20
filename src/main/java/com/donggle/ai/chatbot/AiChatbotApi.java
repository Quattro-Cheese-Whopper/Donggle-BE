package com.donggle.ai.chatbot;

import com.donggle.ai.dto.ChatMemoryResponse;
import com.donggle.ai.dto.ChatRequest;
import com.donggle.ai.dto.ChatResponse;
import com.donggle.ai.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "AI 챗봇", description = "AI 챗봇과의 대화 및 동아리 정보 조회")
public interface AiChatbotApi {

    @Operation(
            summary = "AI 챗봇과 대화",
            description =
                    """
                    AI 챗봇과 대화하며 동아리 관련 정보를 조회할 수 있습니다.

                    **다중 채팅 세션 지원:**
                    - chatId가 없으면 새로운 채팅 세션이 생성됩니다
                    - 기존 chatId를 전달하면 이전 대화를 이어갈 수 있습니다
                    - isNewChat=true로 하면 강제로 새 채팅을 시작합니다

                    **사용 가능한 기능:**
                    - 동아리 검색 및 추천: "컴퓨터 관련 동아리 찾아줘"
                    - 모집 공고 조회: "현재 모집 중인 동아리 알려줘"
                    - 내 지원 현황: "내가 지원한 동아리 상태 확인해줘"
                    - 알림 확인: "새로운 알림 있어?"

                    **예시 질문:**
                    - "전남대 체육 동아리 중에 축구 관련된 곳 추천해줘"
                    - "학술 분과 동아리에서 상시 모집하는 곳 있어?"
                    - "내가 지원한 동아리 중에 합격한 곳 있어?"
                    - "오늘 새로운 공지사항이나 알림 요약해줘"
                    """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "AI 챗봇 응답 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request, @Parameter(hidden = true) Long userId);

    @Operation(summary = "채팅 메모리 초기화", description = "특정 채팅 세션의 메모리를 초기화합니다. 대화 기록이 모두 삭제됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "메모리 초기화 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<ChatMemoryResponse> clearChatMemory(
            @PathVariable String chatId, @Parameter(hidden = true) Long userId);

    @Operation(summary = "AI 챗봇 상태 확인", description = "AI 챗봇 서비스의 동작 상태를 확인합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "서비스 정상 동작"),
        @ApiResponse(responseCode = "500", description = "서비스 오류")
    })
    ResponseEntity<HealthResponse> healthCheck();
}
