package com.donggle.ai.tool;

import com.donggle.ai.service.UserContextHolder;
import com.donggle.domain.club.domain.Club;
import com.donggle.domain.club.dto.ClubResponse;
import com.donggle.domain.club.service.ClubService;
import com.donggle.domain.recruitment.service.ApplicationService;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationTool {

    private final ClubService clubService;
    private final ApplicationService applicationService;

    public record Request(
            String preferences,
            List<String> categories,
            String clubType,
            Boolean excludeApplied,
            Integer limit) {}

    @Tool(
            description =
                    """
            사용자의 관심사와 선호도를 분석하여 맞춤형 동아리를 추천하는 함수입니다.

            ⚠️ **중요: 추천하기 전에 반드시 사용자에게 다음 질문들을 해주세요:**

            🎯 **필수 수집 정보 (이 질문들을 대화로 물어보세요):**
            1. "어떤 분야에 관심이 있으신가요?"
               → 학술, 문화/예술, 체육, 봉사, 종교, 기타 중에서
            2. "중앙동아리와 학과동아리 중 어떤 것을 선호하시나요?"
               → 중앙동아리는 규모가 크고 다양한 전공, 학과동아리는 같은 전공끼리
            3. "어떤 활동을 하고 싶으신가요?"
               → 예: 프로그래밍, 스포츠, 창작활동, 봉사활동, 학습모임 등
            4. "이미 지원한 동아리가 있다면 제외하고 추천드릴까요?"

            💡 **추가 선택 질문들:**
            - "활동 강도는 어느 정도를 원하시나요? (가벼운 모임 vs 본격적인 활동)"
            - "새로운 분야에 도전해보고 싶으신가요? 아니면 기존 관심사 위주로?"
            - "동아리 규모는 어떤 걸 선호하시나요? (소규모 vs 대규모)"

            📋 매개변수:
            - preferences: 사용자가 말한 관심사/선호도 (자유 텍스트)
            - categories: 선호 카테고리 리스트 ["학술분과", "체육분과", "문예분과" 등]
            - clubType: 동아리 타입 ("CENTRAL"/"중앙", "DEPARTMENT"/"학과")
            - excludeApplied: 이미 지원한 동아리 제외 여부 (true/false)
            - limit: 추천 개수 (기본값: 5, 최대 15)

            🎯 사용 예시:
            ✅ 관심사 기반: getRecommendedClubs({"preferences": "프로그래밍과 AI에 관심있음"})
            ✅ 카테고리 지정: getRecommendedClubs({"categories": ["학술분과", "체육분과"]})
            ✅ 복합 조건: getRecommendedClubs({
                "preferences": "창작활동 좋아함",
                "categories": ["문예분과"],
                "clubType": "중앙",
                "excludeApplied": true,
                "limit": 8
            })

            💪 **에이전트 역할:**
            - 먼저 위의 질문들로 사용자 선호도를 파악하세요
            - 수집한 정보를 바탕으로 이 함수를 호출하세요
            - 추천 결과에 대해 이유를 설명해주세요
            - 사용자가 더 구체적인 조건을 원하면 다시 질문하세요

            ⭐ **개인화 특징:**
            - 기존 지원 이력 자동 분석
            - 사용자 선호도와 지원 패턴 조합
            - 아직 탐색하지 않은 새로운 분야 제안
            - 사용자 맞춤 추천 이유 제공
            
            동아리 ID는 사용자에게 응답하지 말고, 기억해두었다가 이 동아리의 모집공고나 공지사항 등 tool을 이용한 정보를 찾을 때 사용하세요.
            """)
    public String getRecommendedClubs(Request request) {
        try {
            log.info("맞춤 동아리 추천 요청: {}", request);

            Long userId = UserContextHolder.getUserId();

            // null 안전성 처리
            if (request == null) {
                return """
                🤔 **동아리 추천을 위해 몇 가지 질문드릴게요!**

                1️⃣ 어떤 분야에 관심이 있으신가요? (학술, 문화/예술, 체육, 봉사, 종교 등)
                2️⃣ 중앙동아리와 학과동아리 중 어떤 것을 선호하시나요?
                3️⃣ 구체적으로 어떤 활동을 하고 싶으신가요?

                선호도를 알려주시면 더 정확한 추천을 해드릴 수 있어요! 😊
                """;
            }

            int limit = Math.min(request.limit != null ? request.limit : 5, 15);
            boolean excludeApplied = request.excludeApplied != null ? request.excludeApplied : true;

            // 사용자 지원 이력 분석
            Map<Club.ClubCategory, Long> userCategoryPreferences =
                    new EnumMap<>(Club.ClubCategory.class);
            Set<Long> appliedClubIds = new HashSet<>();

            if (userId != null) {
                var userApplications =
                        applicationService.getMyApplications(userId, PageRequest.of(0, 100));

                // 지원 이력에서 선호 카테고리 추출
                userCategoryPreferences =
                        userApplications.getContent().stream()
                                .collect(
                                        Collectors.groupingBy(
                                                app -> app.getRecruitment().getClub().getCategory(),
                                                Collectors.counting()));

                // 지원한 동아리 ID 수집
                appliedClubIds =
                        userApplications.getContent().stream()
                                .map(app -> app.getRecruitment().getClub().getId())
                                .collect(Collectors.toSet());
            }

            // 추천 로직 실행
            List<ClubResponse> recommendedClubs =
                    generateRecommendations(
                            request,
                            userCategoryPreferences,
                            appliedClubIds,
                            excludeApplied,
                            limit);

            return formatRecommendationResults(
                    recommendedClubs, request, userCategoryPreferences, userId != null);

        } catch (Exception e) {
            log.error("동아리 추천 중 오류 발생: {}", e.getMessage(), e);
            return "동아리 추천 중 오류가 발생했습니다. 다시 시도해주세요. (오류: " + e.getMessage() + ")";
        }
    }

    private List<ClubResponse> generateRecommendations(
            Request request,
            Map<Club.ClubCategory, Long> userPreferences,
            Set<Long> appliedClubIds,
            boolean excludeApplied,
            int limit) {

        List<ClubResponse> allClubs = clubService.getAllClubs();
        List<ClubResponse> candidates = new ArrayList<>();

        // 1단계: 기본 필터링
        candidates =
                allClubs.stream()
                        .filter(club -> !excludeApplied || !appliedClubIds.contains(club.getId()))
                        .collect(Collectors.toList());

        // 2단계: 동아리 타입 필터링
        if (request.clubType != null && !request.clubType.isBlank()) {
            Club.ClubType targetType = parseClubType(request.clubType);
            candidates =
                    candidates.stream()
                            .filter(club -> club.getType() == targetType)
                            .collect(Collectors.toList());
        }

        // 3단계: 카테고리 필터링
        if (request.categories != null && !request.categories.isEmpty()) {
            Set<Club.ClubCategory> targetCategories =
                    request.categories.stream()
                            .map(this::parseClubCategory)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

            if (!targetCategories.isEmpty()) {
                candidates =
                        candidates.stream()
                                .filter(club -> targetCategories.contains(club.getCategory()))
                                .collect(Collectors.toList());
            }
        }

        // 4단계: 스코어링 및 정렬
        candidates =
                candidates.stream()
                        .sorted(
                                (club1, club2) -> {
                                    double score1 =
                                            calculateRecommendationScore(
                                                    club1, request, userPreferences);
                                    double score2 =
                                            calculateRecommendationScore(
                                                    club2, request, userPreferences);
                                    return Double.compare(score2, score1); // 내림차순
                                })
                        .limit(limit)
                        .collect(Collectors.toList());

        return candidates;
    }

    private double calculateRecommendationScore(
            ClubResponse club, Request request, Map<Club.ClubCategory, Long> userPreferences) {
        double score = 0.0;

        // 1. 기존 지원 이력 기반 점수 (40%)
        if (userPreferences.containsKey(club.getCategory())) {
            score += userPreferences.get(club.getCategory()) * 0.4;
        }

        // 2. 사용자 선호도 텍스트 매칭 (30%)
        if (request.preferences != null && !request.preferences.isBlank()) {
            score += calculatePreferenceMatchScore(club, request.preferences) * 0.3;
        }

        // 3. 동아리 인기도 (20%)
        score += (club.getMemberCount() != null ? club.getMemberCount() : 0) * 0.0002; // 정규화

        // 4. 모집 활성도 (10%)
        if (club.getLatestRecruitmentStatus() != null
                && club.getLatestRecruitmentStatus().name().equals("RECRUITING")) {
            score += 0.1;
        }

        return score;
    }

    private double calculatePreferenceMatchScore(ClubResponse club, String preferences) {
        String lowerPrefs = preferences.toLowerCase();
        String clubName = club.getName().toLowerCase();
        String clubDesc = club.getDescription() != null ? club.getDescription().toLowerCase() : "";
        String categoryName = club.getCategory().name().toLowerCase();

        double matchScore = 0.0;

        // 키워드 매칭
        String[] keywords = {
            "프로그래밍",
            "코딩",
            "개발",
            "컴퓨터",
            "ai",
            "인공지능", // 학술
            "음악",
            "미술",
            "창작",
            "문화",
            "예술",
            "공연",
            "밴드", // 문예
            "축구",
            "농구",
            "운동",
            "스포츠",
            "체육",
            "건강", // 체육
            "봉사",
            "자원봉사",
            "사회활동",
            "도움",
            "나눔", // 봉사
            "기독",
            "불교",
            "종교",
            "신앙",
            "기도" // 종교
        };

        for (String keyword : keywords) {
            if (lowerPrefs.contains(keyword)) {
                if (clubName.contains(keyword) || clubDesc.contains(keyword)) {
                    matchScore += 1.0;
                }
                // 카테고리 매칭
                if (keyword.equals("프로그래밍") || keyword.equals("ai") || keyword.equals("컴퓨터")) {
                    if (categoryName.contains("학술")) matchScore += 0.5;
                } else if (keyword.equals("음악") || keyword.equals("예술")) {
                    if (categoryName.contains("문예")) matchScore += 0.5;
                } else if (keyword.equals("운동") || keyword.equals("스포츠")) {
                    if (categoryName.contains("체육")) matchScore += 0.5;
                } else if (keyword.equals("봉사") && categoryName.contains("봉사")) matchScore += 0.5;
            }
        }

        return Math.min(matchScore, 5.0); // 최대 5점
    }

    private Club.ClubType parseClubType(String type) {
        if (type == null) return null;

        try {
            return Club.ClubType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            String lowerType = type.toLowerCase();
            return switch (lowerType) {
                case "중앙", "중앙동아리", "센트럴", "central" -> Club.ClubType.CENTRAL;
                case "학과", "학과동아리", "과동아리", "department" -> Club.ClubType.DEPARTMENT;
                default -> null;
            };
        }
    }

    private Club.ClubCategory parseClubCategory(String category) {
        if (category == null) return null;

        try {
            return Club.ClubCategory.valueOf(category);
        } catch (IllegalArgumentException e) {
            String lowerCategory = category.toLowerCase();
            if (lowerCategory.contains("학술")) return Club.ClubCategory.학술분과;
            else if (lowerCategory.contains("문예")
                    || lowerCategory.contains("문화")
                    || lowerCategory.contains("예술")) return Club.ClubCategory.문예분과;
            else if (lowerCategory.contains("체육")
                    || lowerCategory.contains("운동")
                    || lowerCategory.contains("스포츠")) return Club.ClubCategory.체육분과;
            else if (lowerCategory.contains("봉사")) return Club.ClubCategory.봉사분과;
            else if (lowerCategory.contains("종교")) return Club.ClubCategory.종교분과;
            else return Club.ClubCategory.기타;
        }
    }

    private String formatRecommendationResults(
            List<ClubResponse> clubs,
            Request request,
            Map<Club.ClubCategory, Long> userPreferences,
            boolean hasUserData) {

        if (clubs.isEmpty()) {
            StringBuilder emptyMessage = new StringBuilder();
            emptyMessage.append("🤔 **조건에 맞는 동아리를 찾지 못했어요.**%n%n");
            emptyMessage.append("💡 다음을 시도해보세요:%n");
            emptyMessage.append("- 조건을 좀 더 넓혀보세요%n");
            emptyMessage.append("- 다른 카테고리도 고려해보세요%n");
            emptyMessage.append("- 새로운 분야에 도전해보는 건 어떨까요?%n");
            return emptyMessage.toString();
        }

        StringBuilder result = new StringBuilder();

        // 헤더
        result.append("🎯 **맞춤 동아리 추천 결과:**%n");
        result.append(String.format("✨ %d개의 추천 동아리를 찾았어요!%n%n", clubs.size()));

        // 추천 기준 설명
        result.append("📊 **추천 기준:**%n");
        if (hasUserData && !userPreferences.isEmpty()) {
            result.append("   🔍 기존 지원 이력 분석 반영%n");
            result.append(
                    String.format(
                            "   📈 주요 관심 분야: %s%n",
                            userPreferences.entrySet().stream()
                                    .sorted(
                                            Map.Entry.<Club.ClubCategory, Long>comparingByValue()
                                                    .reversed())
                                    .limit(2)
                                    .map(entry -> entry.getKey().name())
                                    .collect(Collectors.joining(", "))));
        }
        if (request.preferences != null && !request.preferences.isBlank()) {
            result.append(String.format("   💭 선호도 키워드: %s%n", request.preferences));
        }
        if (request.categories != null && !request.categories.isEmpty()) {
            result.append(
                    String.format("   🎲 지정 카테고리: %s%n", String.join(", ", request.categories)));
        }
        result.append("%n");

        // 추천 동아리 목록
        for (int i = 0; i < clubs.size(); i++) {
            ClubResponse club = clubs.get(i);
            String typeEmoji = getClubTypeEmoji(club.getType());
            String categoryEmoji = getCategoryEmoji(club.getCategory());

            result.append(
                    String.format(
                            "%d. %s%s **%s** (%s)%n",
                            i + 1,
                            typeEmoji,
                            categoryEmoji,
                            club.getName(),
                            club.getCategory().name()));

            // 동아리 ID (사용자에게 보이지 않도록 하기)
            result.append(String.format(" 동아리  ID: %d%n", club.getId()));

            if (club.getDescription() != null && !club.getDescription().isBlank()) {
                String description =
                        club.getDescription().length() > 80
                                ? club.getDescription().substring(0, 80) + "..."
                                : club.getDescription();
                result.append(String.format("   📝 %s%n", description));
            }

            result.append(String.format("   👥 회원수: %d명", club.getMemberCount()));

            if (club.getLocation() != null && !club.getLocation().isBlank()) {
                result.append(String.format(" | 📍 %s", club.getLocation()));
            }

            if (club.getLatestRecruitmentStatus() != null) {
                String status =
                        getRecruitmentStatusKorean(club.getLatestRecruitmentStatus().name());
                String statusEmoji =
                        getRecruitmentStatusEmoji(club.getLatestRecruitmentStatus().name());
                result.append(String.format(" | %s %s", statusEmoji, status));
            }

            result.append("%n");

            // 추천 이유
            String reason = generateRecommendationReason(club, request, userPreferences);
            if (!reason.isEmpty()) {
                result.append(String.format("   💡 **추천 이유**: %s%n", reason));
            }

            result.append("%n");
        }

        // 추가 안내
        result.append("💬 **다음 단계:**%n");
        result.append("- 관심있는 동아리가 있으면 자세한 정보를 확인해보세요%n");
        result.append("- 더 구체적인 조건으로 다시 추천받고 싶으면 말씀해주세요%n");
        result.append("- 새로운 분야도 탐색해보는 것을 추천드려요! 🌟%n");

        return result.toString();
    }

    private String generateRecommendationReason(
            ClubResponse club, Request request, Map<Club.ClubCategory, Long> userPreferences) {
        List<String> reasons = new ArrayList<>();

        // 기존 지원 이력 기반
        if (userPreferences.containsKey(club.getCategory())
                && userPreferences.get(club.getCategory()) > 0) {
            reasons.add("기존에 관심을 보인 " + club.getCategory().name() + " 분야");
        }

        // 선호도 키워드 매칭
        if (request.preferences != null
                && !request.preferences.isBlank()
                && calculatePreferenceMatchScore(club, request.preferences) > 0) {
            reasons.add("선호도와 일치하는 활동 분야");
        }

        // 인기도
        if (club.getMemberCount() != null && club.getMemberCount() > 50) {
            reasons.add("활발한 활동으로 인기 있는 동아리");
        }

        // 모집 상태
        if (club.getLatestRecruitmentStatus() != null
                && club.getLatestRecruitmentStatus().name().equals("RECRUITING")) {
            reasons.add("현재 신입 모집 중");
        }

        return String.join(", ", reasons);
    }

    private String getClubTypeEmoji(Club.ClubType type) {
        return switch (type) {
            case CENTRAL -> "🏛️";
            case DEPARTMENT -> "🎓";
        };
    }

    private String getCategoryEmoji(Club.ClubCategory category) {
        return switch (category) {
            case 학술분과 -> "📚";
            case 문예분과 -> "🎭";
            case 체육분과 -> "⚽";
            case 봉사분과 -> "🤝";
            case 종교분과 -> "🙏";
            default -> "🎯";
        };
    }

    private String getRecruitmentStatusEmoji(String status) {
        return switch (status) {
            case "RECRUITING" -> "🟢";
            case "COMPLETED" -> "🔴";
            case "ALWAYS_RECRUITING" -> "🔵";
            default -> "⚪";
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
