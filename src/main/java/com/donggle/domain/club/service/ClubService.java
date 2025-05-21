package com.donggle.domain.club.service;

import com.donggle.domain.club.domain.Club;
import com.donggle.domain.club.dto.ClubRequest;
import com.donggle.domain.club.dto.ClubResponse;
import com.donggle.domain.club.repository.ClubRepository;
import com.donggle.domain.user.domain.User;
import com.donggle.domain.user.service.UserService;
import com.donggle.global.error.exception.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final UserService userService;

    @Transactional
    public ClubResponse createClub(ClubRequest request, Long userId) {
        if (clubRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("이미 존재하는 동아리명입니다.");
        }

        User user = userService.findById(userId);

        Club club =
                new Club(
                        request.getName(),
                        request.getType(),
                        request.getCategory(),
                        request.getDescription());

        club.addManager(user);

        // 관리자 등록 시 유저의 권한도 매니저로 변경
        userService.updateUserRole(userId, User.UserRole.MANAGER);

        Club savedClub = clubRepository.save(club);
        return ClubResponse.from(savedClub);
    }

    @Transactional
    public ClubResponse updateClub(Long clubId, ClubRequest request, Long userId) {
        Club club = findById(clubId);

        // 해당 동아리의 관리자인지 확인
        validateManager(club, userId);

        club.update(
                request.getName(),
                request.getType(),
                request.getCategory(),
                request.getDescription(),
                request.getMemberCount(),
                request.getLocation(),
                request.getContactInfo());

        Club updatedClub = clubRepository.save(club);
        return ClubResponse.from(updatedClub);
    }

    @Transactional
    public void deleteClub(Long clubId, Long userId) {
        Club club = findById(clubId);

        // 관리자 권한 확인 (어드민은 모든 동아리 삭제 가능)
        User user = userService.findById(userId);
        if (user.getRole() != User.UserRole.ADMIN) {
            validateManager(club, userId);
        }

        clubRepository.delete(club);
    }

    @Transactional(readOnly = true)
    public Club findById(Long id) {
        return clubRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("동아리를 찾을 수 없습니다. ID: " + id));
    }

    @Transactional(readOnly = true)
    public ClubResponse getClub(Long clubId) {
        Club club = findById(clubId);
        return ClubResponse.from(club);
    }

    @Transactional(readOnly = true)
    public List<ClubResponse> getAllClubs() {
        return clubRepository.findAll().stream().map(ClubResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ClubResponse> getClubsByType(Club.ClubType type) {
        return clubRepository.findByType(type).stream().map(ClubResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ClubResponse> getClubsByCategory(Club.ClubCategory category) {
        return clubRepository.findByCategory(category).stream().map(ClubResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Page<ClubResponse> getClubsByTypeAndCategory(
            Club.ClubType type, Club.ClubCategory category, Pageable pageable) {
        return clubRepository
                .findByTypeAndCategory(type, category, pageable)
                .map(ClubResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ClubResponse> searchClubs(String keyword, Pageable pageable) {
        return clubRepository.searchByKeyword(keyword, pageable).map(ClubResponse::from);
    }

    @Transactional(readOnly = true)
    public List<ClubResponse> getClubsByManager(Long userId) {
        User user = userService.findById(userId);
        return clubRepository.findByManager(user).stream().map(ClubResponse::from).toList();
    }

    @Transactional
    public void addManager(Long clubId, Long managerId, Long userId) {
        Club club = findById(clubId);

        // 관리자 권한 확인
        validateManager(club, userId);

        User manager = userService.findById(managerId);
        club.addManager(manager);

        // 매니저 권한 부여
        userService.updateUserRole(managerId, User.UserRole.MANAGER);

        clubRepository.save(club);
    }

    @Transactional
    public void removeManager(Long clubId, Long managerId, Long userId) {
        Club club = findById(clubId);

        // 관리자 권한 확인
        validateManager(club, userId);

        User manager = userService.findById(managerId);
        club.removeManager(manager);

        // 클럽을 더 이상 관리하지 않는다면 일반 유저로 변경
        if (clubRepository.findByManager(manager).isEmpty()) {
            userService.updateUserRole(managerId, User.UserRole.GUEST);
        }

        clubRepository.save(club);
    }

    private void validateManager(Club club, Long userId) {
        User user = userService.findById(userId);

        // 어드민은 모든 권한이 있음
        if (user.getRole() == User.UserRole.ADMIN) {
            return;
        }

        // 매니저 권한 확인
        if (!club.getManagers().contains(user)) {
            throw new IllegalArgumentException("해당 동아리의 관리자가 아닙니다.");
        }
    }
}
