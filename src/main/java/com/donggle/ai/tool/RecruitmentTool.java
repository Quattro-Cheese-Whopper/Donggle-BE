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

    public record Request(
            String action, // "active", "byStatus", "byClub"
            String status,
            Long clubId,
            Integer limit) {
    }

    @Tool(
            description =
                    """
                            모집 공고를 조회하는 함수입니다.
                            
                            매개변수:
                            - action: 조회 방식 ("active"=진행중, "byStatus"=상태별, "byClub"=동아리별)
                            - status: 모집 상태 (RECRUITING=모집중, COMPLETED=모집마감, ALWAYS_RECRUITING=상시모집)
                            - clubId: 동아리 ID (동아리별 조회시 필요)
                            - limit: 결과 개수 제한 (기본값: 5)
                            
                            사용 예시:
                            - "현재 모집 중인 동아리 알려줘" → action="active"
                            - "마감된 모집 공고 보여줘" → action="byStatus", status="COMPLETED"
                            """)
    public String getRecruitments(Request request) {
        try {
            log.info("모집 공고 조회 요청: {}", request);

            List<RecruitmentResponse> recruitments;
            int limit = request.limit != null ? request.limit : 5;

            switch (request.action) {
                case "active" -> recruitments =
                        recruitmentService.getActiveRecruitments().stream()
                                .limit(limit)
                                .toList();
                case "byStatus" -> {
                    Recruitment.RecruitmentStatus recruitmentStatus =
                            parseRecruitmentStatus(request.status);
                    recruitments =
                            recruitmentService.getRecruitmentsByStatus(recruitmentStatus).stream()
                                    .limit(limit)
                                    .toList();
                }
                case "byClub" -> {
                    if (request.clubId == null) {
                        return "동아리 ID가 필요합니다.";
                    }
                    recruitments =
                            recruitmentService.getRecruitmentsByClub(request.clubId).stream()
                                    .limit(limit)
                                    .toList();
                }
                default -> recruitments =
                        recruitmentService.getAllRecruitments().stream()
                                .limit(limit)
                                .toList();
            }

            return formatRecruitmentResults(recruitments, request);

        } catch (Exception e) {
            log.error("모집 공고 조회 중 오류 발생: {}", e.getMessage(), e);
            return "모집 공고 조회 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    private Recruitment.RecruitmentStatus parseRecruitmentStatus(String status) {
        if (status == null) {
            return Recruitment.RecruitmentStatus.RECRUITING;
        }

        try {
            return Recruitment.RecruitmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 한글로 입력된 경우 처리
            return switch (status.toLowerCase()) {
                case "모집중", "모집" -> Recruitment.RecruitmentStatus.RECRUITING;
                case "모집마감", "마감", "완료" -> Recruitment.RecruitmentStatus.COMPLETED;
                case "상시모집" -> Recruitment.RecruitmentStatus.ALWAYS_RECRUITING;
                default -> Recruitment.RecruitmentStatus.RECRUITING;
            };
        }
    }

    private String formatRecruitmentResults(
            List<RecruitmentResponse> recruitments, Request request) {
        if (recruitments.isEmpty()) {
            return getEmptyMessage(request);
        }

        StringBuilder result = new StringBuilder();
        result.append(String.format("모집 공고 %d개:%n%n", recruitments.size()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i < recruitments.size(); i++) {
            RecruitmentResponse recruitment = recruitments.get(i);
            result.append(
                    String.format(
                            "%d. **%s** - %s%n",
                            i + 1, recruitment.getClub().getName(), recruitment.getTitle()));

            result.append(
                    String.format(
                            "   📢 상태: %s%n",
                            getRecruitmentStatusKorean(recruitment.getStatus().name())));

            if (recruitment.getRecruitCount() != null && recruitment.getRecruitCount() > 0) {
                result.append(String.format("   👥 모집인원: %d명%n", recruitment.getRecruitCount()));
            }

            if (recruitment.getStartDate() != null) {
                result.append(
                        String.format(
                                "   📅 시작일: %s%n", recruitment.getStartDate().format(formatter)));
            }

            if (recruitment.getEndDate() != null) {
                result.append(
                        String.format(
                                "   🔚 마감일: %s%n", recruitment.getEndDate().format(formatter)));
            }

            if (recruitment.getContent() != null && !recruitment.getContent().isBlank()) {
                String content =
                        recruitment.getContent().length() > 100
                                ? recruitment.getContent().substring(0, 100) + "..."
                                : recruitment.getContent();
                result.append(String.format("   📝 %s%n", content));
            }

            if (recruitment.getContactInfo() != null && !recruitment.getContactInfo().isBlank()) {
                result.append(String.format("   📞 연락처: %s%n", recruitment.getContactInfo()));
            }

            result.append("%n");
        }

        return result.toString();
    }

    private String getEmptyMessage(Request request) {
        return switch (request.action) {
            case "active" -> "현재 진행 중인 모집 공고가 없습니다.";
            case "byStatus" -> String.format("'%s' 상태의 모집 공고가 없습니다.", request.status);
            case "byClub" -> "해당 동아리의 모집 공고가 없습니다.";
            default -> "조건에 맞는 모집 공고를 찾을 수 없습니다.";
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
