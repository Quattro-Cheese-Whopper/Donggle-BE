package com.donggle.domain.recruitment.service;

import com.donggle.domain.club.domain.Club;
import com.donggle.domain.club.service.ClubService;
import com.donggle.domain.recruitment.domain.Application;
import com.donggle.domain.recruitment.domain.Recruitment;
import com.donggle.domain.recruitment.dto.ApplicationRequest;
import com.donggle.domain.recruitment.dto.ApplicationResponse;
import com.donggle.domain.recruitment.dto.ApplicationStatusRequest;
import com.donggle.domain.recruitment.repository.ApplicationRepository;
import com.donggle.domain.user.domain.User;
import com.donggle.domain.user.service.UserService;
import com.donggle.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final RecruitmentService recruitmentService;
    private final UserService userService;
    private final ClubService clubService;

    @Transactional
    public ApplicationResponse createApplication(
            Long recruitmentId, ApplicationRequest request, Long userId) {
        Recruitment recruitment = recruitmentService.findById(recruitmentId);
        User user = userService.findById(userId);

        // 모집 상태 확인
        if (recruitment.getStatus() != Recruitment.RecruitmentStatus.RECRUITING
                && recruitment.getStatus() != Recruitment.RecruitmentStatus.ALWAYS_RECRUITING) {
            throw new IllegalStateException("현재 모집 중인 동아리가 아닙니다.");
        }

        // 이미 지원한 경우 체크
        if (applicationRepository.existsByRecruitmentAndUser(recruitment, user)) {
            throw new IllegalStateException("이미 해당 모집 공고에 지원했습니다.");
        }

        Application application = new Application(recruitment, user, request.getContent());
        Application savedApplication = applicationRepository.save(application);

        return ApplicationResponse.from(savedApplication);
    }

    @Transactional
    public ApplicationResponse updateApplication(
            Long applicationId, ApplicationRequest request, Long userId) {
        Application application = findById(applicationId);

        // 본인 지원서인지 확인
        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 지원서만 수정할 수 있습니다.");
        }

        // 대기 상태인 경우만 수정 가능
        if (application.getStatus() != Application.ApplicationStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 지원서는 수정할 수 없습니다.");
        }

        application.updateContent(request.getContent());
        Application updatedApplication = applicationRepository.save(application);

        return ApplicationResponse.from(updatedApplication);
    }

    @Transactional
    public void cancelApplication(Long applicationId, Long userId) {
        Application application = findById(applicationId);

        // 본인 지원서인지 확인
        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 지원서만 취소할 수 있습니다.");
        }

        // 대기 상태인 경우만 취소 가능
        if (application.getStatus() != Application.ApplicationStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 지원서는 취소할 수 없습니다.");
        }

        application.updateStatus(Application.ApplicationStatus.CANCELED);
        applicationRepository.save(application);
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(
            Long applicationId, ApplicationStatusRequest request, Long managerId) {
        Application application = findById(applicationId);
        User manager = userService.findById(managerId);

        // 해당 동아리의 관리자인지 확인
        Club club = application.getRecruitment().getClub();
        if (!club.getManagers().contains(manager) && manager.getRole() != User.UserRole.ADMIN) {
            throw new IllegalArgumentException("해당 동아리의 관리자만 지원서 상태를 변경할 수 있습니다.");
        }

        application.updateStatus(request.getStatus());
        Application updatedApplication = applicationRepository.save(application);

        return ApplicationResponse.from(updatedApplication);
    }

    @Transactional(readOnly = true)
    public Application findById(Long id) {
        return applicationRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("지원서를 찾을 수 없습니다. ID: " + id));
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplication(Long applicationId, Long userId) {
        Application application = findById(applicationId);
        User user = userService.findById(userId);

        // 본인의 지원서이거나 해당 동아리 관리자만 조회 가능
        Club club = application.getRecruitment().getClub();
        if (!application.getUser().getId().equals(userId)
                && !club.getManagers().contains(user)
                && user.getRole() != User.UserRole.ADMIN) {
            throw new IllegalArgumentException("해당 지원서를 조회할 권한이 없습니다.");
        }

        return ApplicationResponse.from(application);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplicationsByRecruitment(
            Long recruitmentId, Pageable pageable, Long managerId) {
        Recruitment recruitment = recruitmentService.findById(recruitmentId);
        User manager = userService.findById(managerId);

        // 해당 동아리의 관리자인지 확인
        Club club = recruitment.getClub();
        if (!club.getManagers().contains(manager) && manager.getRole() != User.UserRole.ADMIN) {
            throw new IllegalArgumentException("해당 동아리의 관리자만 지원서 목록을 조회할 수 있습니다.");
        }

        return applicationRepository
                .findByRecruitment(recruitment, pageable)
                .map(ApplicationResponse::briefFrom);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplicationsByClub(
            Long clubId, Pageable pageable, Long managerId) {
        Club club = clubService.findById(clubId);
        User manager = userService.findById(managerId);

        // 해당 동아리의 관리자인지 확인
        if (!club.getManagers().contains(manager) && manager.getRole() != User.UserRole.ADMIN) {
            throw new IllegalArgumentException("해당 동아리의 관리자만 지원서 목록을 조회할 수 있습니다.");
        }

        return applicationRepository
                .findByClubId(clubId, pageable)
                .map(ApplicationResponse::briefFrom);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getMyApplications(Long userId, Pageable pageable) {
        User user = userService.findById(userId);

        return applicationRepository.findByUser(user, pageable).map(ApplicationResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getMyApplicationsByStatus(
            Long userId, Application.ApplicationStatus status, Pageable pageable) {
        User user = userService.findById(userId);

        return applicationRepository
                .findByUserAndStatus(user, status, pageable)
                .map(ApplicationResponse::from);
    }

    @Transactional(readOnly = true)
    public long countApplicationsByRecruitmentAndStatus(
            Long recruitmentId, Application.ApplicationStatus status) {
        Recruitment recruitment = recruitmentService.findById(recruitmentId);
        return applicationRepository.countByRecruitmentAndStatus(recruitment, status);
    }
}
