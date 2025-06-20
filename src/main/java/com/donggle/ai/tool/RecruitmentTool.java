package com.donggle.ai.tool;

import com.donggle.domain.recruitment.domain.Recruitment;
import com.donggle.domain.recruitment.dto.RecruitmentResponse;
import com.donggle.domain.recruitment.service.RecruitmentService;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecruitmentTool {

    private final RecruitmentService recruitmentService;

    public record Request(String status, Long clubId, Boolean activeOnly, Integer limit) {}

    @Tool(
            description =
                    """
            동아리 모집 공고를 유연하게 조회하는 함수입니다. 모든 매개변수는 선택사항이며, 조건을 조합하여 사용할 수 있습니다.

            📋 매개변수 (모두 선택사항):
            - status: 모집 상태 (RECRUITING/모집중, COMPLETED/모집마감, ALWAYS_RECRUITING/상시모집)
            - clubId: 특정 동아리 ID (해당 동아리의 모집 공고만 조회)
            - activeOnly: 진행중인 모집만 조회 (true/false, 기본값: false)
            - limit: 결과 개수 제한 (기본값: 5, 최대 20)

            🎯 사용 예시:
            ✅ 전체 모집공고: getRecruitments({}) 또는 모든 조건 비워서 호출
            ✅ 현재 모집중: getRecruitments({"activeOnly": true}) 또는 getRecruitments({"status": "RECRUITING"})
            ✅ 상태별 조회: getRecruitments({"status": "모집마감"})
            ✅ 특정 동아리: getRecruitments({"clubId": 123})
            ✅ 복합 조건: getRecruitments({"status": "모집중", "limit": 10})
            ✅ 특정 동아리의 진행중 모집: getRecruitments({"clubId": 123, "activeOnly": true})

            💡 팁:
            - 조건이 없으면 전체 모집 공고를 최신순으로 반환합니다
            - activeOnly=true는 현재 모집중인 공고만 필터링합니다
            - 한글/영어 상태명 모두 지원 (예: "모집중" = "RECRUITING")
            - clubId와 다른 조건을 조합하여 더 정확한 검색이 가능합니다
            """)
    public String getRecruitments(Request request) {
        try {
            log.info("모집 공고 조회 요청: {}", request);

            // null 안전성 처리
            if (request == null) {
                request = new Request(null, null, null, null);
            }

            int limit = Math.min(request.limit != null ? request.limit : 5, 20);

            // 조건 정규화
            String status = normalizeString(request.status);
            Boolean activeOnly = request.activeOnly != null ? request.activeOnly : false;

            List<RecruitmentResponse> recruitments =
                    searchWithDynamicConditions(status, request.clubId, activeOnly, limit);
            return formatRecruitmentResults(recruitments, request);

        } catch (Exception e) {
            log.error("모집 공고 조회 중 오류 발생: {}", e.getMessage(), e);
            return "모집 공고 조회 중 오류가 발생했습니다. 조건을 단순화하여 다시 시도해주세요. (오류: " + e.getMessage() + ")";
        }
    }

    /** 동적 조건에 따른 최적 검색 로직 */
    private List<RecruitmentResponse> searchWithDynamicConditions(
            String status, Long clubId, Boolean activeOnly, int limit) {

        // 1. activeOnly가 true면 진행중인 모집만 (가장 일반적인 요청)
        if (activeOnly) {
            log.info("진행중인 모집 공고 조회");
            return recruitmentService.getActiveRecruitments().stream().limit(limit).toList();
        }

        // 2. 특정 동아리 + 상태 조건
        if (clubId != null && hasValue(status)) {
            Recruitment.RecruitmentStatus recruitmentStatus = parseRecruitmentStatus(status);
            log.info("특정 동아리 + 상태별 검색: clubId={}, status={}", clubId, recruitmentStatus);
            return recruitmentService.getRecruitmentsByClub(clubId).stream()
                    .filter(r -> r.getStatus() == recruitmentStatus)
                    .limit(limit)
                    .toList();
        }

        // 3. 특정 동아리의 모집 공고
        if (clubId != null) {
            log.info("특정 동아리 모집 공고 조회: clubId={}", clubId);
            return recruitmentService.getRecruitmentsByClub(clubId).stream().limit(limit).toList();
        }

        // 4. 상태별 조회
        if (hasValue(status)) {
            Recruitment.RecruitmentStatus recruitmentStatus = parseRecruitmentStatus(status);
            log.info("상태별 모집 공고 조회: status={}", recruitmentStatus);
            return recruitmentService.getRecruitmentsByStatus(recruitmentStatus).stream()
                    .limit(limit)
                    .toList();
        }

        // 5. 전체 조회 (최신순)
        log.info("전체 모집 공고 조회");
        return recruitmentService.getAllRecruitments().stream().limit(limit).toList();
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalizeString(String value) {
        return value != null ? value.trim() : null;
    }

    private Recruitment.RecruitmentStatus parseRecruitmentStatus(String status) {
        if (status == null) {
            return Recruitment.RecruitmentStatus.RECRUITING;
        }

        try {
            return Recruitment.RecruitmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 한글 및 자연어 처리 (더 포괄적)
            String lowerStatus = status.toLowerCase();
            return switch (lowerStatus) {
                case "모집중", "모집", "recruiting", "진행중", "활성" ->
                        Recruitment.RecruitmentStatus.RECRUITING;
                case "모집마감", "마감", "완료", "종료", "completed", "끝" ->
                        Recruitment.RecruitmentStatus.COMPLETED;
                case "상시모집", "상시", "always", "always_recruiting", "연중" ->
                        Recruitment.RecruitmentStatus.ALWAYS_RECRUITING;
                default -> {
                    log.warn("알 수 없는 모집 상태: {}. RECRUITING으로 기본 설정", status);
                    yield Recruitment.RecruitmentStatus.RECRUITING;
                }
            };
        }
    }

    private String formatRecruitmentResults(
            List<RecruitmentResponse> recruitments, Request request) {
        if (recruitments.isEmpty()) {
            StringBuilder emptyMessage = new StringBuilder();
            emptyMessage.append("🔍 검색 조건에 맞는 모집 공고를 찾을 수 없습니다.%n%n");
            emptyMessage.append("💡 다음을 시도해보세요:%n");
            emptyMessage.append("- 모집 상태를 다르게 설정해보세요%n");
            emptyMessage.append("- 전체 모집 공고를 확인해보세요%n");
            emptyMessage.append("- 다른 동아리의 모집 공고를 찾아보세요%n");
            return emptyMessage.toString();
        }

        StringBuilder result = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 검색 조건 요약
        result.append("📢 **모집 공고 검색 결과:**%n");
        result.append(String.format("📊 총 %d개 모집 공고 발견%n", recruitments.size()));

        if (hasValue(request.status)) result.append(String.format("📋 상태: %s%n", request.status));
        if (request.clubId != null) result.append(String.format("🏢 동아리 ID: %d%n", request.clubId));
        if (request.activeOnly != null && request.activeOnly) result.append("⚡ 진행중인 모집만 표시%n");
        result.append("%n");

        // 상태별 통계
        long recruitingCount =
                recruitments.stream()
                        .filter(r -> r.getStatus() == Recruitment.RecruitmentStatus.RECRUITING)
                        .count();
        long completedCount =
                recruitments.stream()
                        .filter(r -> r.getStatus() == Recruitment.RecruitmentStatus.COMPLETED)
                        .count();
        long alwaysCount =
                recruitments.stream()
                        .filter(
                                r ->
                                        r.getStatus()
                                                == Recruitment.RecruitmentStatus.ALWAYS_RECRUITING)
                        .count();

        if (recruitments.size() > 1) {
            result.append("📈 **상태별 현황:**%n");
            result.append(
                    String.format(
                            "   🟢 모집중: %d개 | 🔴 마감: %d개 | 🔵 상시모집: %d개%n%n",
                            recruitingCount, completedCount, alwaysCount));
        }

        // 모집 공고 목록
        for (int i = 0; i < recruitments.size(); i++) {
            RecruitmentResponse recruitment = recruitments.get(i);
            String statusEmoji = getStatusEmoji(recruitment.getStatus());

            result.append(
                    String.format(
                            "%d. %s **%s** - %s%n",
                            i + 1,
                            statusEmoji,
                            recruitment.getClub().getName(),
                            recruitment.getTitle()));

            result.append(
                    String.format(
                            "   📢 상태: %s%n",
                            getRecruitmentStatusKorean(recruitment.getStatus().name())));

            if (recruitment.getRecruitCount() != null && recruitment.getRecruitCount() > 0) {
                result.append(String.format("   👥 모집인원: %d명%n", recruitment.getRecruitCount()));
            }

            // 기간 정보
            if (recruitment.getStartDate() != null && recruitment.getEndDate() != null) {
                result.append(
                        String.format(
                                "   📅 모집기간: %s ~ %s%n",
                                recruitment.getStartDate().format(formatter),
                                recruitment.getEndDate().format(formatter)));
            } else if (recruitment.getStartDate() != null) {
                result.append(
                        String.format(
                                "   📅 시작일: %s%n", recruitment.getStartDate().format(formatter)));
            } else if (recruitment.getEndDate() != null) {
                result.append(
                        String.format(
                                "   🔚 마감일: %s%n", recruitment.getEndDate().format(formatter)));
            }

            if (recruitment.getContent() != null && !recruitment.getContent().isBlank()) {
                String content =
                        recruitment.getContent().length() > 80
                                ? recruitment.getContent().substring(0, 80) + "..."
                                : recruitment.getContent();
                result.append(String.format("   📝 %s%n", content));
            }

            if (recruitment.getContactInfo() != null && !recruitment.getContactInfo().isBlank()) {
                result.append(String.format("   📞 연락처: %s%n", recruitment.getContactInfo()));
            }

            result.append("%n");
        }

        // 추가 안내
        if (recruitingCount > 0 && !request.activeOnly) {
            result.append("💡 현재 지원 가능한 모집이 있습니다!%n");
        }
        if (recruitments.size() >= (request.limit != null ? request.limit : 5)) {
            result.append("💡 더 많은 결과를 보려면 limit 값을 늘려주세요!%n");
        }

        return result.toString();
    }

    private String getStatusEmoji(Recruitment.RecruitmentStatus status) {
        return switch (status) {
            case RECRUITING -> "🟢";
            case COMPLETED -> "🔴";
            case ALWAYS_RECRUITING -> "🔵";
        };
    }

    private String getRecruitmentStatusKorean(String status) {
        return switch (status) {
            case "RECRUITING" -> "모집중";
            case "COMPLETED" -> "모집마감";
            case "ALWAYS_RECRUITING" -> "상시모집";
            default -> status;
        };
    }
}
