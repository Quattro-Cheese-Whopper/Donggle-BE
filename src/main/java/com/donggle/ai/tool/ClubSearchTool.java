package com.donggle.ai.tool;

import com.donggle.domain.club.domain.Club;
import com.donggle.domain.club.dto.ClubResponse;
import com.donggle.domain.club.service.ClubService;
import com.donggle.domain.recruitment.domain.Recruitment;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClubSearchTool {

    private final ClubService clubService;

    public record Request(String keyword, String type, String category, Integer limit) {}

    public record RecruitingRequest(String type, String category, Integer limit) {}

    @Tool(
            description =
                    """
            동아리를 검색하는 함수입니다.
            사용자가 원하는 조건에 맞는 동아리를 파라미터로 전달해 검색하는 것을 최우선순위로 두세요.
            카테고리가 제공된 경우(학술동아리, 문예 동아리 등) 무조건 category 조건에 넣어(학술, 문예 등) 검색하세요. (예: "학술분과", "문예분과")

            📋 매개변수:
            - keyword: 검색할 키워드 (동아리명, 설명 등에서 검색)
            - type: 동아리 타입 (CENTRAL/중앙/중앙동아리, DEPARTMENT/학과/학과동아리)
            - category: 동아리 카테고리 (학술분과, 문예분과, 체육분과, 봉사분과, 종교분과, 기타)
            - limit: 결과 개수 제한 (기본값: 5, 최대 20)

            🎯 사용 예시:
            ✅ 전체 조회: searchClubs({}) 또는 모든 조건 비워서 호출
            ✅ 키워드 검색: searchClubs({"keyword": "컴퓨터"})
            ✅ 카테고리별: searchClubs({"category": "체육분과"})
            ✅ 타입별: searchClubs({"type": "CENTRAL"})
            ✅ 복합 조건: searchClubs({"type": "중앙", "category": "학술분과", "limit": 10})
            ✅ 키워드 + 타입: searchClubs({"keyword": "프로그래밍", "type": "학과"})

            💡 팁:
            - 한글/영어 모두 지원 (예: "중앙" = "CENTRAL")
            - 키워드는 동아리명과 설명에서 부분 일치로 검색됩니다
            - 조건 조합으로 더 정확한 검색이 가능합니다
            - "학술 동아리" 라면 "학술분과"로 카테고리 설정하듯이 "분과"가 생략된 경우 카테고리에서 "분과"를 추가해 검색하세요
            - "중앙, 학과"만 동아리 타입이고, "학술", "문예" 등 나머지 단어가 들어간 경우 모두 분과로 취급해 검색하세요

            동아리 ID는 사용자에게 응답하지 말고, 기억해두었다가 이 동아리의 모집공고나 공지사항 등 tool을 이용한 정보를 찾을 때 사용하세요.
            """)
    public String searchClubs(Request request) {
        try {
            log.info("동아리 검색 요청: {}", request);

            // null 안전성 처리
            if (request == null) {
                request = new Request(null, null, null, null);
            }

            int limit = Math.min(request.limit != null ? request.limit : 5, 20);

            // 조건 정규화
            String keyword = normalizeString(request.keyword);
            String type = normalizeString(request.type);
            String category = normalizeString(request.category);

            List<ClubResponse> clubs = searchWithDynamicConditions(keyword, type, category, limit);
            return formatClubResults(clubs, request);

        } catch (Exception e) {
            log.error("동아리 검색 중 오류 발생: {}", e.getMessage(), e);
            return "동아리 검색 중 오류가 발생했습니다. 조건을 단순화하여 다시 시도해주세요. (오류: " + e.getMessage() + ")";
        }
    }

    @Tool(
            description =
                    """
            현재 모집중인 동아리만을 조회하는 함수입니다. 모집중(RECRUITING) 또는 상시모집(ALWAYS_RECRUITING) 상태인 동아리만 반환합니다.

            📋 매개변수 (모두 선택사항):
            - type: 동아리 타입 (CENTRAL/중앙/중앙동아리, DEPARTMENT/학과/학과동아리)
            - category: 동아리 카테고리 (학술분과, 문예분과, 체육분과, 봉사분과, 종교분과, 기타)
            - limit: 결과 개수 제한 (기본값: 10, 최대 20)

            🎯 사용 예시:
            ✅ 모집중인 모든 동아리: getRecruitingClubs({}) 또는 조건 없이 호출
            ✅ 중앙동아리만: getRecruitingClubs({"type": "중앙"})
            ✅ 체육분과만: getRecruitingClubs({"category": "체육분과"})
            ✅ 복합 조건: getRecruitingClubs({"type": "학과", "category": "학술분과"})
            ✅ 제한된 결과: getRecruitingClubs({"limit": 5})

            💡 특징:
            - 모집중(RECRUITING) + 상시모집(ALWAYS_RECRUITING) 상태만 필터링
            - 사용자가 "지금 가입할 수 있는 동아리"를 찾을 때 최적
            - 타입과 카테고리로 추가 필터링 가능
            - 한글/영어 타입명 모두 지원 (예: "중앙" = "CENTRAL")

            ⚡ 언제 사용하나요?
            - "지금 가입할 수 있는 동아리가 뭐가 있어?"
            - "모집중인 체육동아리 보여줘"
            - "현재 신입생을 받고 있는 학과동아리는?"
            - "상시모집하는 동아리 있어?"

            동아리 ID는 사용자에게 응답하지 말고, 기억해두었다가 이 동아리의 모집공고나 공지사항 등 tool을 이용한 정보를 찾을 때 사용하세요.
            """)
    public String getRecruitingClubs(RecruitingRequest request) {
        try {
            log.info("모집중인 동아리 조회 요청: {}", request);

            // null 안전성 처리
            if (request == null) {
                request = new RecruitingRequest(null, null, null);
            }

            int limit = Math.min(request.limit != null ? request.limit : 10, 20);

            // 조건 정규화
            String type = normalizeString(request.type);
            String category = normalizeString(request.category);

            List<ClubResponse> clubs = getRecruitingClubsWithConditions(type, category, limit);
            return formatRecruitingClubResults(clubs, request);

        } catch (Exception e) {
            log.error("모집중인 동아리 조회 중 오류 발생: {}", e.getMessage(), e);
            return "모집중인 동아리 조회 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    /** 동적 조건에 따른 최적 검색 로직 */
    private List<ClubResponse> searchWithDynamicConditions(
            String keyword, String type, String category, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);

        // 1. 키워드가 있으면 키워드 검색 우선 (가장 구체적)
        if (hasValue(keyword)) {
            log.info("키워드 검색 실행: {}", keyword);
            return clubService.searchClubs(keyword, pageRequest).getContent();
        }

        // 2. 타입과 카테고리 둘 다 있으면 복합 조건 검색
        if (hasValue(type) && hasValue(category)) {
            Club.ClubType clubType = parseClubType(type);
            Club.ClubCategory clubCategory = parseClubCategory(category);
            log.info("복합 조건 검색 실행: type={}, category={}", clubType, clubCategory);
            return clubService
                    .getClubsByTypeAndCategory(clubType, clubCategory, pageRequest)
                    .getContent();
        }

        // 3. 타입만 있으면 타입별 검색
        if (hasValue(type)) {
            Club.ClubType clubType = parseClubType(type);
            log.info("타입별 검색 실행: {}", clubType);
            return clubService.getClubsByType(clubType).stream().limit(limit).toList();
        }

        // 4. 카테고리만 있으면 카테고리별 검색
        if (hasValue(category)) {
            Club.ClubCategory clubCategory = parseClubCategory(category);
            log.info("카테고리별 검색 실행: {}", clubCategory);
            return clubService.getClubsByCategory(clubCategory).stream().limit(limit).toList();
        }

        // 5. 조건이 없으면 전체 조회 (성능 최적화된 방식)
        log.info("전체 동아리 조회 실행");
        return clubService.getAllClubs().stream().limit(limit).toList();
    }

    /** 모집중인 동아리 조회 로직 */
    private List<ClubResponse> getRecruitingClubsWithConditions(
            String type, String category, int limit) {
        // 1. 타입과 카테고리 둘 다 있으면 복합 조건 검색
        if (hasValue(type) && hasValue(category)) {
            Club.ClubType clubType = parseClubType(type);
            Club.ClubCategory clubCategory = parseClubCategory(category);
            log.info("모집중인 동아리 복합 조건 검색: type={}, category={}", clubType, clubCategory);
            return clubService.getRecruitingClubsByType(clubType).stream()
                    .filter(club -> club.getCategory() == clubCategory)
                    .limit(limit)
                    .toList();
        }

        // 2. 타입만 있으면 타입별 모집중인 동아리 검색
        if (hasValue(type)) {
            Club.ClubType clubType = parseClubType(type);
            log.info("타입별 모집중인 동아리 검색: {}", clubType);
            return clubService.getRecruitingClubsByType(clubType).stream().limit(limit).toList();
        }

        // 3. 카테고리만 있으면 카테고리별 모집중인 동아리 검색
        if (hasValue(category)) {
            Club.ClubCategory clubCategory = parseClubCategory(category);
            log.info("카테고리별 모집중인 동아리 검색: {}", clubCategory);
            return clubService.getRecruitingClubsByCategory(clubCategory).stream()
                    .limit(limit)
                    .toList();
        }

        // 4. 조건이 없으면 모집중인 모든 동아리 조회
        log.info("모집중인 모든 동아리 조회");
        return clubService.getRecruitingClubs().stream().limit(limit).toList();
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalizeString(String value) {
        return value != null ? value.trim() : null;
    }

    private Club.ClubType parseClubType(String type) {
        if (type == null) return null;

        try {
            return Club.ClubType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 한글 및 자연어 처리
            String lowerType = type.toLowerCase();
            return switch (lowerType) {
                case "중앙", "중앙동아리", "센트럴", "central" -> Club.ClubType.CENTRAL;
                case "학과", "학과동아리", "과동아리", "department" -> Club.ClubType.DEPARTMENT;
                default -> {
                    log.warn("알 수 없는 동아리 타입: {}. CENTRAL로 기본 설정", type);
                    yield Club.ClubType.CENTRAL;
                }
            };
        }
    }

    private Club.ClubCategory parseClubCategory(String category) {
        if (category == null) return null;

        try {
            return Club.ClubCategory.valueOf(category);
        } catch (IllegalArgumentException e) {
            // 키워드 매칭으로 카테고리 추정 (더 포괄적)
            String lowerCategory = category.toLowerCase();
            if (lowerCategory.contains("학술")
                    || lowerCategory.contains("공부")
                    || lowerCategory.contains("컴퓨터")
                    || lowerCategory.contains("프로그래밍")
                    || lowerCategory.contains("연구")
                    || lowerCategory.contains("학습")) {
                return Club.ClubCategory.학술분과;
            } else if (lowerCategory.contains("문화")
                    || lowerCategory.contains("문예")
                    || lowerCategory.contains("예술")
                    || lowerCategory.contains("음악")
                    || lowerCategory.contains("미술")
                    || lowerCategory.contains("창작")) {
                return Club.ClubCategory.문예분과;
            } else if (lowerCategory.contains("체육")
                    || lowerCategory.contains("운동")
                    || lowerCategory.contains("스포츠")
                    || lowerCategory.contains("축구")
                    || lowerCategory.contains("농구")
                    || lowerCategory.contains("테니스")) {
                return Club.ClubCategory.체육분과;
            } else if (lowerCategory.contains("봉사")
                    || lowerCategory.contains("자원봉사")
                    || lowerCategory.contains("사회활동")
                    || lowerCategory.contains("도움")) {
                return Club.ClubCategory.봉사분과;
            } else if (lowerCategory.contains("종교")
                    || lowerCategory.contains("신앙")
                    || lowerCategory.contains("기독")
                    || lowerCategory.contains("불교")) {
                return Club.ClubCategory.종교분과;
            } else {
                log.warn("알 수 없는 카테고리: {}. 기타로 설정", category);
                return Club.ClubCategory.기타;
            }
        }
    }

    private String formatClubResults(List<ClubResponse> clubs, Request request) {
        if (clubs.isEmpty()) {
            StringBuilder emptyMessage = new StringBuilder();
            emptyMessage.append("🔍 검색 조건에 맞는 동아리를 찾을 수 없습니다.%n%n");
            emptyMessage.append("💡 다음을 시도해보세요:%n");
            emptyMessage.append("- 키워드를 더 간단하게 바꿔보세요%n");
            emptyMessage.append("- 조건을 일부 제거해보세요%n");
            emptyMessage.append("- 전체 동아리 목록을 확인해보세요%n");
            return emptyMessage.toString();
        }

        StringBuilder result = new StringBuilder();

        // 검색 조건 요약
        result.append("🎯 **검색 결과 요약:**%n");
        result.append(String.format("📊 총 %d개 동아리 발견%n", clubs.size()));

        if (hasValue(request.keyword))
            result.append(String.format("🔍 키워드: %s%n", request.keyword));
        if (hasValue(request.type)) result.append(String.format("🏢 타입: %s%n", request.type));
        if (hasValue(request.category))
            result.append(String.format("📂 카테고리: %s%n", request.category));
        result.append("%n");

        // 동아리 목록
        for (int i = 0; i < clubs.size(); i++) {
            ClubResponse club = clubs.get(i);
            result.append(
                    String.format(
                            "%d. **%s** (%s - %s)%n",
                            i + 1,
                            club.getName(),
                            getClubTypeKorean(club.getType().name()),
                            club.getCategory().name()));

            // 동아리 ID (사용자에게는 응답하지 않음)
            result.append(String.format("   (추후 작업에서 활용할 수 있는 동아리 ID: %d)%n", club.getId()));

            if (club.getDescription() != null && !club.getDescription().isBlank()) {
                String description =
                        club.getDescription().length() > 60
                                ? club.getDescription().substring(0, 60) + "..."
                                : club.getDescription();
                result.append(String.format("   📝 %s%n", description));
            }

            result.append(String.format("   👥 회원수: %d명%n", club.getMemberCount()));

            if (club.getLocation() != null && !club.getLocation().isBlank()) {
                result.append(String.format("   📍 위치: %s%n", club.getLocation()));
            }

            if (club.getLatestRecruitmentStatus() != null) {
                result.append(
                        String.format(
                                "   📢 모집상태: %s%n",
                                getRecruitmentStatusKorean(
                                        club.getLatestRecruitmentStatus().name())));
            }

            result.append("%n");
        }

        // 추가 안내
        if (clubs.size() >= (request.limit != null ? request.limit : 5)) {
            result.append("💡 더 많은 결과를 보려면 limit 값을 늘려주세요!%n");
        }

        return result.toString();
    }

    private String formatRecruitingClubResults(
            List<ClubResponse> clubs, RecruitingRequest request) {
        if (clubs.isEmpty()) {
            StringBuilder emptyMessage = new StringBuilder();
            emptyMessage.append("🔍 현재 모집중인 동아리를 찾을 수 없습니다.%n%n");
            emptyMessage.append("💡 다음을 시도해보세요:%n");
            emptyMessage.append("- 다른 타입이나 카테고리로 검색해보세요%n");
            emptyMessage.append("- 전체 동아리 목록에서 확인해보세요%n");
            emptyMessage.append("- 나중에 다시 확인해보세요 (모집 일정이 변경될 수 있습니다)%n");
            return emptyMessage.toString();
        }

        StringBuilder result = new StringBuilder();

        // 검색 조건 요약
        result.append("🔥 **모집중인 동아리 검색 결과:**%n");
        result.append(String.format("📊 현재 가입 가능한 동아리 %d개%n", clubs.size()));

        if (hasValue(request.type)) result.append(String.format("🏢 타입: %s%n", request.type));
        if (hasValue(request.category))
            result.append(String.format("📂 카테고리: %s%n", request.category));
        result.append("%n");

        // 모집상태별 통계
        long recruitingCount =
                clubs.stream()
                        .filter(club -> club.getLatestRecruitmentStatus() != null)
                        .filter(
                                club ->
                                        club.getLatestRecruitmentStatus()
                                                .name()
                                                .equals("RECRUITING"))
                        .count();
        long alwaysRecruitingCount =
                clubs.stream()
                        .filter(club -> club.getLatestRecruitmentStatus() != null)
                        .filter(
                                club ->
                                        club.getLatestRecruitmentStatus()
                                                .name()
                                                .equals("ALWAYS_RECRUITING"))
                        .count();

        if (clubs.size() > 1) {
            result.append("📈 **모집 현황:**%n");
            result.append(
                    String.format(
                            "   🚀 모집중: %d개 | ⏰ 상시모집: %d개%n%n",
                            recruitingCount, alwaysRecruitingCount));
        }

        // 동아리 목록
        for (int i = 0; i < clubs.size(); i++) {
            ClubResponse club = clubs.get(i);
            String recruitmentIcon = getRecruitmentIcon(club.getLatestRecruitmentStatus());

            result.append(
                    String.format(
                            "%d. %s**%s** (%s - %s)%n",
                            i + 1,
                            recruitmentIcon,
                            club.getName(),
                            getClubTypeKorean(club.getType().name()),
                            club.getCategory().name()));

            // 동아리 ID (사용자에게는 응답하지 않음)
            result.append(String.format("   (추후 작업에서 활용할 수 있는 동아리 ID: %d)%n", club.getId()));

            if (club.getDescription() != null && !club.getDescription().isBlank()) {
                String description =
                        club.getDescription().length() > 80
                                ? club.getDescription().substring(0, 80) + "..."
                                : club.getDescription();
                result.append(String.format("   📝 %s%n", description));
            }

            result.append(String.format("   👥 회원수: %d명%n", club.getMemberCount()));

            if (club.getLocation() != null && !club.getLocation().isBlank()) {
                result.append(String.format("   📍 위치: %s%n", club.getLocation()));
            }

            if (club.getLatestRecruitmentStatus() != null) {
                result.append(
                        String.format(
                                "   ✨ 모집상태: %s%n",
                                getRecruitmentStatusKorean(
                                        club.getLatestRecruitmentStatus().name())));
            }

            result.append("%n");
        }

        // 추가 안내
        result.append("🎯 **참고사항:**%n");
        result.append("- 🚀 모집중: 정해진 기간 동안 신입생을 모집하는 동아리%n");
        result.append("- ⏰ 상시모집: 언제든지 가입 가능한 동아리%n");

        if (clubs.size() >= (request.limit != null ? request.limit : 10)) {
            result.append("%n💡 더 많은 결과를 보려면 limit 값을 늘려주세요!%n");
        }

        return result.toString();
    }

    private String getClubTypeKorean(String type) {
        return switch (type) {
            case "CENTRAL" -> "중앙동아리";
            case "DEPARTMENT" -> "학과동아리";
            default -> type;
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

    private String getRecruitmentIcon(Recruitment.RecruitmentStatus status) {
        if (status == null) return "❓";
        return switch (status.name()) {
            case "RECRUITING" -> "🚀";
            case "ALWAYS_RECRUITING" -> "⏰";
            case "COMPLETED" -> "📋";
            default -> "❓";
        };
    }
}
