package com.donggle.ai.tool;

import com.donggle.domain.club.domain.Club;
import com.donggle.domain.club.dto.ClubResponse;
import com.donggle.domain.club.service.ClubService;
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

    @Tool(
            description =
                    """
            동아리를 검색하고 추천하는 함수입니다.

            매개변수:
            - keyword: 검색할 키워드 (동아리명, 설명 등에서 검색)
            - type: 동아리 타입 (CENTRAL=중앙동아리, DEPARTMENT=학과동아리)
            - category: 동아리 카테고리 (학술분과, 문예분과, 체육분과, 봉사분과, 종교분과, 기타 등)
            - limit: 결과 개수 제한 (기본값: 5)

            사용 예시:
            - "컴퓨터 관련 동아리 찾아줘" → keyword="컴퓨터"
            - "체육 동아리 추천해줘" → category="체육분과"
            - "중앙동아리 중에서 학술 관련" → type="CENTRAL", category="학술분과"
            """)
    public String searchClubs(Request request) {
        try {
            log.info("동아리 검색 요청: {}", request);

            List<ClubResponse> clubs;
            if (request == null) {
                return "검색 요청이 유효하지 않습니다.";
            }
            int limit = request.limit != null ? request.limit : 5;

            if (request.keyword != null && !request.keyword.isBlank()) {
                // 키워드로 검색
                var page = clubService.searchClubs(request.keyword, PageRequest.of(0, limit));
                clubs = page.getContent();
            } else if (request.type != null) {
                // 타입별 조회
                Club.ClubType clubType = parseClubType(request.type);
                if (request.category != null) {
                    Club.ClubCategory clubCategory = parseClubCategory(request.category);
                    var page =
                            clubService.getClubsByTypeAndCategory(
                                    clubType, clubCategory, PageRequest.of(0, limit));
                    clubs = page.getContent();
                } else {
                    clubs = clubService.getClubsByType(clubType).stream().limit(limit).toList();
                }
            } else if (request.category != null) {
                // 카테고리별 조회
                Club.ClubCategory clubCategory = parseClubCategory(request.category);
                clubs = clubService.getClubsByCategory(clubCategory).stream().limit(limit).toList();
            } else {
                // 전체 조회
                clubs = clubService.getAllClubs().stream().limit(limit).toList();
            }

            return formatClubResults(clubs);

        } catch (Exception e) {
            log.error("동아리 검색 중 오류 발생: {}", e.getMessage(), e);
            return "동아리 검색 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    private Club.ClubType parseClubType(String type) {
        try {
            return Club.ClubType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 한글로 입력된 경우 처리
            return switch (type.toLowerCase()) {
                case "중앙", "중앙동아리" -> Club.ClubType.CENTRAL;
                case "학과", "학과동아리" -> Club.ClubType.DEPARTMENT;
                default -> Club.ClubType.CENTRAL; // 기본값
            };
        }
    }

    private Club.ClubCategory parseClubCategory(String category) {
        try {
            return Club.ClubCategory.valueOf(category);
        } catch (IllegalArgumentException e) {
            // 키워드 매칭으로 카테고리 추정
            String lowerCategory = category.toLowerCase();
            if (lowerCategory.contains("학술")
                    || lowerCategory.contains("공부")
                    || lowerCategory.contains("컴퓨터")) {
                return Club.ClubCategory.학술분과;
            } else if (lowerCategory.contains("문화") || lowerCategory.contains("문예")) {
                return Club.ClubCategory.문예분과;
            } else if (lowerCategory.contains("체육") || lowerCategory.contains("운동")) {
                return Club.ClubCategory.체육분과;
            } else if (lowerCategory.contains("봉사")) {
                return Club.ClubCategory.봉사분과;
            } else if (lowerCategory.contains("종교")) {
                return Club.ClubCategory.종교분과;
            } else {
                return Club.ClubCategory.기타; // 기본값
            }
        }
    }

    private String formatClubResults(List<ClubResponse> clubs) {
        if (clubs.isEmpty()) {
            return "검색 조건에 맞는 동아리를 찾을 수 없습니다.";
        }

        StringBuilder result = new StringBuilder();
        result.append(String.format("검색된 동아리 %d개:%n%n", clubs.size()));

        for (int i = 0; i < clubs.size(); i++) {
            ClubResponse club = clubs.get(i);
            result.append(
                    String.format(
                            "%d. **%s** (%s - %s)%n",
                            i + 1,
                            club.getName(),
                            club.getType().name(),
                            club.getCategory().name()));

            if (club.getDescription() != null && !club.getDescription().isBlank()) {
                result.append(
                        String.format(
                                "   📝 %s%n",
                                club.getDescription().length() > 50
                                        ? club.getDescription().substring(0, 50) + "..."
                                        : club.getDescription()));
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

        return result.toString();
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
