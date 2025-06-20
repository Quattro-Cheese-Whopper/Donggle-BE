package com.donggle.ai.tool;

import com.donggle.ai.service.UserContextHolder;
import com.donggle.domain.recruitment.domain.Application;
import com.donggle.domain.recruitment.dto.ApplicationResponse;
import com.donggle.domain.recruitment.service.ApplicationService;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationTool {

    private final ApplicationService applicationService;

    public record Request(String status, Integer limit) {}

    @Tool(
            description =
                    """
            사용자의 지원서 목록을 조회하는 함수입니다.

            매개변수:
            - status: 지원 상태 (PENDING=대기중, APPROVED=합격, REJECTED=불합격, CANCELED=취소)
            - limit: 결과 개수 제한 (기본값: 10)

            사용 예시:
            - "내가 지원한 동아리 상태 알려줘" → 전체 조회
            - "합격한 동아리 있어?" → status="APPROVED"
            - "대기 중인 지원서 확인해줘" → status="PENDING"
            """)
    public String getMyApplications(Request request) {
        try {
            log.info("지원서 조회 요청: {}", request);

            Long userId = UserContextHolder.getUserId();
            if (userId == null) {
                return "사용자 ID를 확인할 수 없습니다.";
            }

            int limit = request.limit != null ? request.limit : 10;
            List<ApplicationResponse> applications;

            if (request.status != null && !request.status.isBlank()) {
                Application.ApplicationStatus applicationStatus =
                        parseApplicationStatus(request.status);
                var page =
                        applicationService.getMyApplicationsByStatus(
                                userId, applicationStatus, PageRequest.of(0, limit));
                applications = page.getContent();
            } else {
                var page = applicationService.getMyApplications(userId, PageRequest.of(0, limit));
                applications = page.getContent();
            }

            return formatApplicationResults(applications, request);

        } catch (Exception e) {
            log.error("지원서 조회 중 오류 발생: {}", e.getMessage(), e);
            return "지원서 조회 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    private Application.ApplicationStatus parseApplicationStatus(String status) {
        try {
            return Application.ApplicationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 한글로 입력된 경우 처리
            return switch (status.toLowerCase()) {
                case "대기", "대기중", "대기 중" -> Application.ApplicationStatus.PENDING;
                case "합격", "승인", "통과" -> Application.ApplicationStatus.APPROVED;
                case "불합격", "거절", "탈락" -> Application.ApplicationStatus.REJECTED;
                case "취소", "철회" -> Application.ApplicationStatus.CANCELED;
                default -> Application.ApplicationStatus.PENDING;
            };
        }
    }

    private String formatApplicationResults(
            List<ApplicationResponse> applications, Request request) {
        if (applications.isEmpty()) {
            String statusMessage =
                    request.status != null
                            ? String.format("'%s' 상태의 ", getApplicationStatusKorean(request.status))
                            : "";
            return String.format("%s지원서가 없습니다.", statusMessage);
        }

        StringBuilder result = new StringBuilder();
        result.append(String.format("내 지원서 %d개:%n%n", applications.size()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 상태별 통계
        long pendingCount =
                applications.stream()
                        .filter(app -> app.getStatus() == Application.ApplicationStatus.PENDING)
                        .count();
        long approvedCount =
                applications.stream()
                        .filter(app -> app.getStatus() == Application.ApplicationStatus.APPROVED)
                        .count();
        long rejectedCount =
                applications.stream()
                        .filter(app -> app.getStatus() == Application.ApplicationStatus.REJECTED)
                        .count();
        long canceledCount =
                applications.stream()
                        .filter(app -> app.getStatus() == Application.ApplicationStatus.CANCELED)
                        .count();

        result.append("📊 **상태별 요약:**%n");
        result.append(
                String.format(
                        "   ⏳ 대기중: %d개 | ✅ 합격: %d개 | ❌ 불합격: %d개 | 🚫 취소: %d개%n%n",
                        pendingCount, approvedCount, rejectedCount, canceledCount));

        for (int i = 0; i < applications.size(); i++) {
            ApplicationResponse app = applications.get(i);
            String statusEmoji = getStatusEmoji(app.getStatus());

            result.append(
                    String.format(
                            "%d. %s **%s** - %s%n",
                            i + 1,
                            statusEmoji,
                            app.getRecruitment().getClub().getName(),
                            app.getRecruitment().getTitle()));

            result.append(String.format("   📅 지원일: %s%n", app.getCreatedAt().format(formatter)));

            result.append(
                    String.format(
                            "   📝 상태: %s%n", getApplicationStatusKorean(app.getStatus().name())));

            if (app.getUpdatedAt() != null && !app.getUpdatedAt().equals(app.getCreatedAt())) {
                result.append(
                        String.format("   🔄 상태 변경일: %s%n", app.getUpdatedAt().format(formatter)));
            }

            // 지원서 내용 일부 표시
            if (app.getContent() != null && !app.getContent().isBlank()) {
                String content =
                        app.getContent().length() > 80
                                ? app.getContent().substring(0, 80) + "..."
                                : app.getContent();
                result.append(String.format("   💭 지원동기: %s%n", content));
            }

            result.append("%n");
        }

        // 추가 인사이트 제공
        if (pendingCount > 0) {
            result.append("💡 **안내**: 대기 중인 지원서가 있습니다. 결과를 기다려주세요!%n");
        }
        if (approvedCount > 0) {
            result.append("🎉 **축하합니다**: 합격한 동아리가 있습니다!%n");
        }

        return result.toString();
    }

    private String getStatusEmoji(Application.ApplicationStatus status) {
        return switch (status) {
            case PENDING -> "⏳";
            case APPROVED -> "✅";
            case REJECTED -> "❌";
            case CANCELED -> "🚫";
        };
    }

    private String getApplicationStatusKorean(String status) {
        return switch (status.toUpperCase()) {
            case "PENDING" -> "대기중";
            case "APPROVED" -> "합격";
            case "REJECTED" -> "불합격";
            case "CANCELED" -> "취소";
            default -> status;
        };
    }
}
