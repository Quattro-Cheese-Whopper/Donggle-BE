package com.donggle.ai.tool;

import com.donggle.domain.announce.domain.Announce;
import com.donggle.domain.announce.dto.AnnounceResponse;
import com.donggle.domain.announce.service.AnnounceService;
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
public class AnnounceTool {

    private final AnnounceService announceService;

    public static final String DATE_FORMAT = "MM-dd HH:mm";

    public record Request(String type, Long clubId, Integer limit) {}

    @Tool(
            description =
                    """
            공지사항을 유연하게 조회하는 함수입니다. 모든 매개변수는 선택사항이며, 조건을 조합하여 사용할 수 있습니다.

            📋 매개변수 (모두 선택사항):
            - type: 공지사항 타입 (GENERAL/일반, CLUB/동아리)
            - clubId: 특정 동아리 ID (해당 동아리의 공지사항만 조회)
            - limit: 결과 개수 제한 (기본값: 10, 최대 30)

            🎯 사용 예시:
            ✅ 전체 공지사항: getAnnouncements({}) 또는 모든 조건 비워서 호출
            ✅ 일반 공지사항: getAnnouncements({"type": "GENERAL"}) 또는 getAnnouncements({"type": "일반"})
            ✅ 동아리 공지사항: getAnnouncements({"type": "CLUB"}) 또는 getAnnouncements({"type": "동아리"})
            ✅ 특정 동아리: getAnnouncements({"clubId": 123})
            ✅ 복합 조건: getAnnouncements({"type": "동아리", "limit": 15})
            ✅ 특정 동아리의 공지: getAnnouncements({"clubId": 123, "limit": 5})

            💡 팁:
            - 조건이 없으면 최근 일반 공지사항을 기본으로 반환합니다
            - 한글/영어 타입명 모두 지원 (예: "일반" = "GENERAL")
            - clubId와 다른 조건을 조합하여 더 정확한 검색이 가능합니다
            - 고정된 중요 공지사항은 📌 아이콘으로 표시됩니다
            """)
    public String getAnnouncements(Request request) {
        try {
            log.info("공지사항 조회 요청: {}", request);

            // null 안전성 처리
            if (request == null) {
                request = new Request(null, null, null);
            }

            int limit = Math.min(request.limit != null ? request.limit : 10, 30);

            // 조건 정규화
            String type = normalizeString(request.type);

            List<AnnounceResponse> announcements =
                    searchWithDynamicConditions(type, request.clubId, limit);
            return formatAnnouncementResults(announcements, request);

        } catch (Exception e) {
            log.error("공지사항 조회 중 오류 발생: {}", e.getMessage(), e);
            return "공지사항 조회 중 오류가 발생했습니다. 조건을 단순화하여 다시 시도해주세요. (오류: " + e.getMessage() + ")";
        }
    }

    @Tool(
            description =
                    """
            최근 공지사항을 조회하는 함수입니다. 최신순으로 정렬된 공지사항을 빠르게 확인할 수 있습니다.

            📋 매개변수 (모두 선택사항):
            - type: 공지사항 타입 (GENERAL/일반, CLUB/동아리)
            - clubId: 특정 동아리 ID
            - limit: 결과 개수 제한 (기본값: 5, 최대 15)

            🎯 사용 예시:
            ✅ 최근 공지사항: getRecentAnnouncements({}) 또는 조건 없이 호출
            ✅ 최근 일반 공지: getRecentAnnouncements({"type": "일반"})
            ✅ 최근 동아리 공지: getRecentAnnouncements({"type": "동아리"})
            ✅ 특정 동아리 최근 공지: getRecentAnnouncements({"clubId": 123})
            ✅ 더 많은 결과: getRecentAnnouncements({"limit": 10})

            💡 특징:
            - 최신순 정렬로 가장 새로운 공지부터 표시
            - 빠른 조회를 위해 최적화된 함수
            - 중요 공지사항은 📌 아이콘으로 하이라이트
            - 최근 활동 파악에 유용
            """)
    public String getRecentAnnouncements(Request request) {
        try {
            log.info("최근 공지사항 조회 요청: {}", request);

            // null 안전성 처리
            if (request == null) {
                request = new Request(null, null, null);
            }

            int limit = Math.min(request.limit != null ? request.limit : 5, 15);

            // 조건 정규화
            String type = normalizeString(request.type);

            List<AnnounceResponse> announcements =
                    getRecentWithConditions(type, request.clubId, limit);
            return formatRecentAnnouncementResults(announcements, request);

        } catch (Exception e) {
            log.error("최근 공지사항 조회 중 오류 발생: {}", e.getMessage(), e);
            return "최근 공지사항 조회 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    /** 동적 조건에 따른 최적 검색 로직 */
    private List<AnnounceResponse> searchWithDynamicConditions(
            String type, Long clubId, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);

        // 1. 특정 동아리 + 타입 조건
        if (clubId != null && hasValue(type)) {
            log.info("특정 동아리 + 타입별 검색: clubId={}, type={}", clubId, type);
            var page = announceService.getAnnouncesByClub(clubId, pageRequest);
            Announce.AnnounceType announceType = parseAnnounceType(type);
            return page.getContent().stream()
                    .filter(a -> a.getType() == announceType)
                    .limit(limit)
                    .toList();
        }

        // 2. 특정 동아리의 공지사항
        if (clubId != null) {
            log.info("특정 동아리 공지사항 조회: clubId={}", clubId);
            return announceService.getAnnouncesByClub(clubId, pageRequest).getContent();
        }

        // 3. 타입별 조회
        if (hasValue(type)) {
            Announce.AnnounceType announceType = parseAnnounceType(type);
            log.info("타입별 공지사항 조회: type={}", announceType);
            return announceService.getAnnouncesByType(announceType, pageRequest).getContent();
        }

        // 4. 기본 조회 (일반 공지사항)
        log.info("기본 공지사항 조회 (일반 공지)");
        return announceService
                .getAnnouncesByType(Announce.AnnounceType.GENERAL, pageRequest)
                .getContent();
    }

    /** 최근 공지사항 조회 로직 */
    private List<AnnounceResponse> getRecentWithConditions(String type, Long clubId, int limit) {
        // 1. 특정 동아리의 최근 공지사항
        if (clubId != null) {
            log.info("특정 동아리 최근 공지사항 조회: clubId={}", clubId);
            return announceService.getRecentAnnouncesByClub(clubId).stream().limit(limit).toList();
        }

        // 2. 타입별 최근 공지사항
        if (hasValue(type)) {
            Announce.AnnounceType announceType = parseAnnounceType(type);
            log.info("타입별 최근 공지사항 조회: type={}", announceType);
            return announceService.getRecentAnnouncesByType(announceType).stream()
                    .limit(limit)
                    .toList();
        }

        // 3. 기본 최근 공지사항 (일반 공지)
        log.info("기본 최근 공지사항 조회 (일반 공지)");
        return announceService.getRecentAnnouncesByType(Announce.AnnounceType.GENERAL).stream()
                .limit(limit)
                .toList();
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalizeString(String value) {
        return value != null ? value.trim() : null;
    }

    private Announce.AnnounceType parseAnnounceType(String type) {
        if (type == null) {
            return Announce.AnnounceType.GENERAL;
        }

        try {
            return Announce.AnnounceType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 한글 및 자연어 처리
            String lowerType = type.toLowerCase();
            return switch (lowerType) {
                case "일반", "전체", "general", "공지", "공통" -> Announce.AnnounceType.GENERAL;
                case "동아리", "클럽", "club", "소모임" -> Announce.AnnounceType.CLUB;
                default -> {
                    log.warn("알 수 없는 공지사항 타입: {}. GENERAL로 기본 설정", type);
                    yield Announce.AnnounceType.GENERAL;
                }
            };
        }
    }

    private String formatAnnouncementResults(
            List<AnnounceResponse> announcements, Request request) {
        if (announcements.isEmpty()) {
            StringBuilder emptyMessage = new StringBuilder();
            emptyMessage.append("🔍 검색 조건에 맞는 공지사항을 찾을 수 없습니다.%n%n");
            emptyMessage.append("💡 다음을 시도해보세요:%n");
            emptyMessage.append("- 공지사항 타입을 다르게 설정해보세요%n");
            emptyMessage.append("- 전체 공지사항을 확인해보세요%n");
            emptyMessage.append("- 다른 동아리의 공지사항을 찾아보세요%n");
            return emptyMessage.toString();
        }

        StringBuilder result = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        // 검색 조건 요약
        result.append("📢 **공지사항 검색 결과:**%n");
        result.append(String.format("📊 총 %d개 공지사항 발견%n", announcements.size()));

        if (hasValue(request.type)) result.append(String.format("📋 타입: %s%n", request.type));
        if (request.clubId != null) result.append(String.format("🏢 동아리 ID: %d%n", request.clubId));
        result.append("%n");

        // 타입별 통계
        long generalCount =
                announcements.stream()
                        .filter(a -> a.getType() == Announce.AnnounceType.GENERAL)
                        .count();
        long clubCount =
                announcements.stream()
                        .filter(a -> a.getType() == Announce.AnnounceType.CLUB)
                        .count();

        if (announcements.size() > 1) {
            result.append("📈 **타입별 현황:**%n");
            result.append(
                    String.format("   📋 일반공지: %d개 | 🏢 동아리공지: %d개%n%n", generalCount, clubCount));
        }

        // 공지사항 목록
        for (int i = 0; i < announcements.size(); i++) {
            AnnounceResponse announcement = announcements.get(i);
            String typeEmoji = getTypeEmoji(announcement.getType());
            String pinnedIcon = announcement.isPinned() ? "📌 " : "";

            result.append(
                    String.format(
                            "%d. %s%s%s**%s**%n",
                            i + 1,
                            pinnedIcon,
                            typeEmoji,
                            announcement.getClubName() != null
                                    ? "[" + announcement.getClubName() + "] "
                                    : "",
                            announcement.getTitle()));

            result.append(
                    String.format(
                            "   📅 %s | ✍️ %s%n",
                            announcement.getCreatedAt().format(formatter),
                            announcement.getAuthorName()));

            if (announcement.getContent() != null && !announcement.getContent().isBlank()) {
                String content =
                        announcement.getContent().length() > 100
                                ? announcement.getContent().substring(0, 100) + "..."
                                : announcement.getContent();
                result.append(String.format("   📝 %s%n", content));
            }

            result.append("%n");
        }

        // 추가 안내
        if (announcements.stream().anyMatch(AnnounceResponse::isPinned)) {
            result.append("📌 중요한 고정 공지사항이 있습니다!%n");
        }
        if (announcements.size() >= (request.limit != null ? request.limit : 10)) {
            result.append("💡 더 많은 결과를 보려면 limit 값을 늘려주세요!%n");
        }

        return result.toString();
    }

    private String formatRecentAnnouncementResults(
            List<AnnounceResponse> announcements, Request request) {
        if (announcements.isEmpty()) {
            String typeMessage =
                    request.type != null
                            ? String.format("'%s' 타입의 ", getAnnounceTypeKorean(request.type))
                            : "";
            String clubMessage =
                    request.clubId != null ? String.format("동아리 ID %d의 ", request.clubId) : "";
            return String.format("최근 %s%s공지사항이 없습니다.", clubMessage, typeMessage);
        }

        StringBuilder result = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        // 헤더
        result.append("🔥 **최근 공지사항:**%n");
        result.append(String.format("📊 %d개의 최신 공지%n", announcements.size()));

        if (hasValue(request.type))
            result.append(String.format("📋 타입: %s%n", getAnnounceTypeKorean(request.type)));
        if (request.clubId != null) result.append(String.format("🏢 동아리 ID: %d%n", request.clubId));
        result.append("%n");

        // 공지사항 목록 (최신순)
        for (int i = 0; i < announcements.size(); i++) {
            AnnounceResponse announcement = announcements.get(i);
            String typeEmoji = getTypeEmoji(announcement.getType());
            String pinnedIcon = announcement.isPinned() ? "📌 " : "";

            result.append(
                    String.format(
                            "%d. %s%s%s**%s**%n",
                            i + 1,
                            pinnedIcon,
                            typeEmoji,
                            announcement.getClubName() != null
                                    ? "[" + announcement.getClubName() + "] "
                                    : "",
                            announcement.getTitle()));

            result.append(
                    String.format(
                            "   ⏰ %s | ✍️ %s%n",
                            announcement.getCreatedAt().format(formatter),
                            announcement.getAuthorName()));

            if (announcement.getContent() != null && !announcement.getContent().isBlank()) {
                String content =
                        announcement.getContent().length() > 80
                                ? announcement.getContent().substring(0, 80) + "..."
                                : announcement.getContent();
                result.append(String.format("   💬 %s%n", content));
            }

            result.append("%n");
        }

        // 추가 정보
        long pinnedCount = announcements.stream().mapToLong(a -> a.isPinned() ? 1 : 0).sum();
        if (pinnedCount > 0) {
            result.append(String.format("📌 고정된 중요 공지가 %d개 있습니다!%n", pinnedCount));
        }

        return result.toString();
    }

    private String getTypeEmoji(Announce.AnnounceType type) {
        return switch (type) {
            case GENERAL -> "📋";
            case CLUB -> "🏢";
        };
    }

    private String getAnnounceTypeKorean(String type) {
        if (type == null) return "전체";

        try {
            Announce.AnnounceType announceType = parseAnnounceType(type);
            return switch (announceType) {
                case GENERAL -> "일반공지";
                case CLUB -> "동아리공지";
            };
        } catch (Exception e) {
            return type;
        }
    }
}
