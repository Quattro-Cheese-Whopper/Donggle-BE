package com.donggle.domain.recruitment.service;

import com.donggle.domain.club.domain.Club;
import com.donggle.domain.club.service.ClubService;
import com.donggle.domain.recruitment.domain.Recruitment;
import com.donggle.domain.recruitment.dto.RecruitmentRequest;
import com.donggle.domain.recruitment.dto.RecruitmentResponse;
import com.donggle.domain.recruitment.repository.RecruitmentRepository;
import com.donggle.domain.user.domain.User;
import com.donggle.domain.user.service.UserService;
import com.donggle.global.error.exception.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final RecruitmentRepository recruitmentRepository;
    private final ClubService clubService;
    private final UserService userService;

    @Transactional
    public RecruitmentResponse createRecruitment(
            Long clubId, RecruitmentRequest request, Long userId) {
        Club club = clubService.findById(clubId);

        // 해당 동아리의 관리자인지 확인
        User user = userService.findById(userId);
        validateManager(club, user);

        Recruitment recruitment =
                new Recruitment(
                        club,
                        request.getTitle(),
                        request.getContent(),
                        request.getRecruitCount(),
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getStatus(),
                        request.getContactInfo(),
                        request.getApplicationLink());

        Recruitment savedRecruitment = recruitmentRepository.save(recruitment);
        return RecruitmentResponse.from(savedRecruitment);
    }

    @Transactional
    public RecruitmentResponse updateRecruitment(
            Long recruitmentId, RecruitmentRequest request, Long userId) {
        Recruitment recruitment = findById(recruitmentId);

        // 해당 동아리의 관리자인지 확인
        User user = userService.findById(userId);
        validateManager(recruitment.getClub(), user);

        recruitment.update(
                request.getTitle(),
                request.getContent(),
                request.getRecruitCount(),
                request.getStartDate(),
                request.getEndDate(),
                request.getStatus(),
                request.getContactInfo(),
                request.getApplicationLink());

        Recruitment updatedRecruitment = recruitmentRepository.save(recruitment);
        return RecruitmentResponse.from(updatedRecruitment);
    }

    @Transactional
    public void deleteRecruitment(Long recruitmentId, Long userId) {
        Recruitment recruitment = findById(recruitmentId);

        // 해당 동아리의 관리자인지 확인
        User user = userService.findById(userId);
        validateManager(recruitment.getClub(), user);

        recruitmentRepository.delete(recruitment);
    }

    @Transactional(readOnly = true)
    public Recruitment findById(Long id) {
        return recruitmentRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("모집 공고를 찾을 수 없습니다. ID: " + id));
    }

    @Transactional(readOnly = true)
    public RecruitmentResponse getRecruitment(Long recruitmentId) {
        Recruitment recruitment = findById(recruitmentId);
        return RecruitmentResponse.from(recruitment);
    }

    @Transactional(readOnly = true)
    public List<RecruitmentResponse> getAllRecruitments() {
        return recruitmentRepository.findAll().stream().map(RecruitmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<RecruitmentResponse> getActiveRecruitments() {
        return recruitmentRepository.findActiveRecruitments(LocalDateTime.now()).stream()
                .map(RecruitmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecruitmentResponse> getRecruitmentsByClub(Long clubId) {
        Club club = clubService.findById(clubId);
        return recruitmentRepository.findByClub(club).stream()
                .map(RecruitmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecruitmentResponse> getRecruitmentsByStatus(Recruitment.RecruitmentStatus status) {
        return recruitmentRepository.findByStatus(status).stream()
                .map(RecruitmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecruitmentResponse> getRecruitmentsByClubType(
            Club.ClubType type, Recruitment.RecruitmentStatus status) {
        return recruitmentRepository.findByClubTypeAndStatus(type, status).stream()
                .map(RecruitmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecruitmentResponse> getRecruitmentsByClubCategory(
            Club.ClubCategory category, Recruitment.RecruitmentStatus status) {
        return recruitmentRepository.findByClubCategoryAndStatus(category, status).stream()
                .map(RecruitmentResponse::from)
                .toList();
    }

    @Transactional
    public void updateRecruitmentStatus(
            Long recruitmentId, Recruitment.RecruitmentStatus status, Long userId) {
        Recruitment recruitment = findById(recruitmentId);

        // 해당 동아리의 관리자인지 확인
        User user = userService.findById(userId);
        validateManager(recruitment.getClub(), user);

        recruitment.updateStatus(status);
        recruitmentRepository.save(recruitment);
    }

    private void validateManager(Club club, User user) {
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
