package com.donggle.domain.announce.service;

import com.donggle.domain.announce.domain.Announce;
import com.donggle.domain.announce.dto.AnnounceRequest;
import com.donggle.domain.announce.dto.AnnounceResponse;
import com.donggle.domain.announce.repository.AnnounceRepository;
import com.donggle.domain.club.domain.Club;
import com.donggle.domain.club.service.ClubService;
import com.donggle.domain.notification.domain.Notification;
import com.donggle.domain.notification.service.NotificationService;
import com.donggle.domain.user.domain.User;
import com.donggle.domain.user.service.UserService;
import com.donggle.global.error.exception.EntityNotFoundException;
import com.donggle.global.error.exception.ForbiddenException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnnounceService {

    private final AnnounceRepository announceRepository;
    private final UserService userService;
    private final ClubService clubService;
    private final NotificationService notificationService;

    @Transactional
    public AnnounceResponse createAnnounce(AnnounceRequest request, Long userId, Long clubId) {
        User author = userService.findById(userId);
        Club club = clubId != null ? clubService.findById(clubId) : null;

        // 권한 체크
        if (club != null && !club.isManager(author)) {
            throw new ForbiddenException("해당 동아리의 관리자가 아닙니다.");
        }

        Announce announce =
                new Announce(
                        author, club, request.getTitle(), request.getContent(), request.isPinned());
        Announce savedAnnounce = announceRepository.save(announce);

        // 알림 생성 (동아리 공지인 경우)
        if (club != null) {
            notificationService.createNotificationForClubMembers(
                    club,
                    "새로운 동아리 공지사항",
                    club.getName() + "에 새로운 공지사항이 등록되었습니다: " + announce.getTitle(),
                    Notification.NotificationType.NEW_ANNOUNCE,
                    savedAnnounce.getId());
        }

        return AnnounceResponse.from(savedAnnounce);
    }

    @Transactional
    public AnnounceResponse updateAnnounce(Long announceId, AnnounceRequest request, Long userId) {
        Announce announce = findById(announceId);
        User user = userService.findById(userId);

        // 권한 체크
        if (!announce.isAuthor(user) && !announce.isClubManager(user) && !user.isAdmin()) {
            throw new ForbiddenException("공지사항을 수정할 권한이 없습니다.");
        }

        announce.update(request.getTitle(), request.getContent(), request.isPinned());
        Announce updatedAnnounce = announceRepository.save(announce);

        return AnnounceResponse.from(updatedAnnounce);
    }

    @Transactional
    public void deleteAnnounce(Long announceId, Long userId) {
        Announce announce = findById(announceId);
        User user = userService.findById(userId);

        // 권한 체크
        if (!announce.isAuthor(user) && !announce.isClubManager(user) && !user.isAdmin()) {
            throw new ForbiddenException("공지사항을 삭제할 권한이 없습니다.");
        }

        announceRepository.delete(announce);
    }

    @Transactional(readOnly = true)
    public Announce findById(Long id) {
        return announceRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을 수 없습니다. ID: " + id));
    }

    @Transactional(readOnly = true)
    public AnnounceResponse getAnnounce(Long announceId) {
        Announce announce = findById(announceId);
        return AnnounceResponse.from(announce);
    }

    @Transactional(readOnly = true)
    public Page<AnnounceResponse> getAnnouncesByType(
            Announce.AnnounceType type, Pageable pageable) {
        return announceRepository.findByType(type, pageable).map(AnnounceResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AnnounceResponse> getAnnouncesByClub(Long clubId, Pageable pageable) {
        Club club = clubService.findById(clubId);

        return announceRepository.findByClub(club, pageable).map(AnnounceResponse::from);
    }

    @Transactional(readOnly = true)
    public List<AnnounceResponse> getRecentAnnouncesByType(Announce.AnnounceType type) {
        return announceRepository.findTop5ByTypeOrderByCreatedAtDesc(type).stream()
                .map(AnnounceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AnnounceResponse> getRecentAnnouncesByClub(Long clubId) {
        Club club = clubService.findById(clubId);

        return announceRepository.findTop5ByClubOrderByCreatedAtDesc(club).stream()
                .map(AnnounceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AnnounceResponse> getAnnouncesByClubName(String clubName, Pageable pageable) {
        Club club = clubService.findByName(clubName);

        return announceRepository.findByClub(club, pageable).map(AnnounceResponse::from);
    }

    @Transactional(readOnly = true)
    public List<AnnounceResponse> getRecentAnnouncesByClubName(String clubName) {
        Club club = clubService.findByName(clubName);

        return announceRepository.findTop5ByClubOrderByCreatedAtDesc(club).stream()
                .map(AnnounceResponse::from)
                .toList();
    }
}
