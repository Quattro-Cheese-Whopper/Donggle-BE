package com.donggle.ai.tool;

import com.donggle.ai.service.UserContextHolder;
import com.donggle.domain.notification.dto.NotificationResponse;
import com.donggle.domain.notification.service.NotificationService;
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
public class NotificationTool {

    private final NotificationService notificationService;

    public static final String USER_ID_ERROR = "사용자 ID를 확인할 수 없습니다.";
    public static final String DATE_FORMAT = "MM-dd HH:mm";

    public record Request(
            String action, // "unread", "recent", "summary", "count"
            Integer limit) {}

    @Tool(
            description =
                    """
            미읽은 알림 목록을 조회하는 함수입니다. 실시간 알림 현황을 제공합니다.

            📋 매개변수 (모두 선택사항):
            - limit: 결과 개수 제한 (기본값: 5, 최대 20)

            🎯 사용 예시:
            ✅ 새로운 알림 확인: getUnreadNotifications({})
            ✅ 더 많은 알림: getUnreadNotifications({"limit": 10})

            💡 팁:
            - 읽지 않은 알림만 최신순으로 보여줍니다
            - 알림 타입별로 이모지와 함께 표시됩니다
            - 개인정보이므로 로그인한 사용자만 조회 가능합니다
            """)
    public String getUnreadNotifications(Request request) {
        try {
            log.info("미읽은 알림 조회 요청: {}", request);

            Long userId = UserContextHolder.getUserId();
            if (userId == null) {
                return USER_ID_ERROR;
            }

            int limit = Math.min(request.limit != null ? request.limit : 5, 20);
            var page = notificationService.getUnreadNotifications(userId, PageRequest.of(0, limit));
            List<NotificationResponse> notifications = page.getContent();

            if (notifications.isEmpty()) {
                return "새로운 알림이 없습니다. 📭";
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("📬 **미읽은 알림 %d개:**%n%n", notifications.size()));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

            for (int i = 0; i < notifications.size(); i++) {
                NotificationResponse notification = notifications.get(i);
                String typeEmoji = getNotificationTypeEmoji(notification.getType().name());

                result.append(
                        String.format(
                                "%d. %s **%s**%n", i + 1, typeEmoji, notification.getTitle()));
                result.append(
                        String.format("   📅 %s%n", notification.getCreatedAt().format(formatter)));
                result.append(String.format("   💬 %s%n%n", notification.getContent()));
            }

            return result.toString();

        } catch (Exception e) {
            log.error("미읽은 알림 조회 중 오류 발생: {}", e.getMessage(), e);
            return "미읽은 알림 조회 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    @Tool(
            description =
                    """
            알림을 요약해서 제공하는 스마트 알림 대시보드 함수입니다.

            📋 매개변수: 없음 (모든 미읽은 알림을 자동 분석)

            🎯 제공 정보:
            ✅ 미읽은 알림 총 개수
            ✅ 카테고리별 분류 (공지사항, 지원결과, 새 모집, 동아리 등)
            ✅ 우선순위 높은 중요 알림 하이라이트
            ✅ 알림 현황 한눈에 보기

            💡 사용 시나리오:
            - "알림 요약해줘" - 전체 알림 현황 파악
            - "중요한 알림 알려줘" - 놓친 중요 알림 확인
            - "놓친 공지사항 있나?" - 공지사항 위주 확인
            """)
    public String getNotificationSummary(Request request) {
        try {
            log.info("알림 요약 조회 요청: {}", request);

            Long userId = UserContextHolder.getUserId();
            if (userId == null) {
                return USER_ID_ERROR;
            }

            long unreadCount = notificationService.countUnreadNotifications(userId);
            List<NotificationResponse> recentNotifications =
                    notificationService.getRecentUnreadNotifications(userId);

            StringBuilder result = new StringBuilder();
            result.append("📊 **알림 요약:**%n%n");
            result.append(String.format("📬 미읽은 알림: %d개%n", unreadCount));

            if (unreadCount == 0) {
                result.append("🎉 모든 알림을 확인하셨습니다!%n");
                return result.toString();
            }

            // 타입별 분류
            long announceCount =
                    recentNotifications.stream()
                            .filter(n -> "NEW_ANNOUNCE".equals(n.getType().name()))
                            .count();
            long applicationCount =
                    recentNotifications.stream()
                            .filter(n -> "APPLICATION_STATUS_CHANGE".equals(n.getType().name()))
                            .count();
            long recruitmentCount =
                    recentNotifications.stream()
                            .filter(n -> "NEW_RECRUITMENT".equals(n.getType().name()))
                            .count();
            long clubCount =
                    recentNotifications.stream()
                            .filter(n -> "CLUB_MANAGER_ADDED".equals(n.getType().name()))
                            .count();

            result.append("%n📋 **카테고리별:**%n");
            if (announceCount > 0) result.append(String.format("   📢 공지사항: %d개%n", announceCount));
            if (applicationCount > 0)
                result.append(String.format("   📝 지원결과: %d개%n", applicationCount));
            if (recruitmentCount > 0)
                result.append(String.format("   🎯 새 모집: %d개%n", recruitmentCount));
            if (clubCount > 0) result.append(String.format("   👥 동아리: %d개%n", clubCount));

            // 우선순위 높은 알림 표시
            if (!recentNotifications.isEmpty()) {
                result.append("%n🔥 **중요 알림:**%n");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

                for (int i = 0; i < Math.min(3, recentNotifications.size()); i++) {
                    NotificationResponse notification = recentNotifications.get(i);
                    String typeEmoji = getNotificationTypeEmoji(notification.getType().name());
                    result.append(
                            String.format(
                                    "   %s %s (%s)%n",
                                    typeEmoji,
                                    notification.getTitle(),
                                    notification.getCreatedAt().format(formatter)));
                }
            }

            return result.toString();

        } catch (Exception e) {
            log.error("알림 요약 조회 중 오류 발생: {}", e.getMessage(), e);
            return "알림 요약 조회 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    @Tool(
            description =
                    """
            최근 알림을 전체적으로 조회하는 함수입니다. 읽음/안읽음 상태 구분하여 표시합니다.

            📋 매개변수 (모두 선택사항):
            - limit: 결과 개수 제한 (기본값: 10, 최대 30)

            🎯 사용 예시:
            ✅ 최근 알림 전체: getRecentNotifications({})
            ✅ 더 많은 알림: getRecentNotifications({"limit": 20})

            💡 특징:
            - 읽음/안읽음 상태를 아이콘으로 구분 (✅/🔴)
            - 알림 타입별 이모지 표시
            - 최신순 정렬로 최근 활동 파악 가능
            - 전체 알림 이력 확인에 유용
            """)
    public String getRecentNotifications(Request request) {
        try {
            log.info("최근 알림 조회 요청: {}", request);

            Long userId = UserContextHolder.getUserId();
            if (userId == null) {
                return USER_ID_ERROR;
            }

            int limit = Math.min(request.limit != null ? request.limit : 10, 30);
            var page = notificationService.getNotifications(userId, PageRequest.of(0, limit));
            List<NotificationResponse> notifications = page.getContent();

            if (notifications.isEmpty()) {
                return "알림이 없습니다.";
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("📬 **전체 알림 %d개:**%n%n", notifications.size()));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

            for (int i = 0; i < notifications.size(); i++) {
                NotificationResponse notification = notifications.get(i);
                String typeEmoji = getNotificationTypeEmoji(notification.getType().name());
                String readStatus = notification.isRead() ? "✅" : "🔴";

                result.append(
                        String.format(
                                "%d. %s %s %s%n",
                                i + 1, readStatus, typeEmoji, notification.getTitle()));
                result.append(
                        String.format("   📅 %s%n", notification.getCreatedAt().format(formatter)));
                result.append(String.format("   💬 %s%n%n", notification.getContent()));
            }

            return result.toString();

        } catch (Exception e) {
            log.error("최근 알림 조회 중 오류 발생: {}", e.getMessage(), e);
            return "최근 알림 조회 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    private String getNotificationTypeEmoji(String type) {
        return switch (type) {
            case "NEW_ANNOUNCE" -> "📢";
            case "APPLICATION_STATUS_CHANGE" -> "📝";
            case "NEW_RECRUITMENT" -> "🎯";
            case "CLUB_MANAGER_ADDED" -> "👥";
            default -> "🔔";
        };
    }
}
