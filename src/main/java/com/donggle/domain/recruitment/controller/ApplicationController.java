package com.donggle.domain.recruitment.controller;

import com.donggle.domain.recruitment.domain.Application;
import com.donggle.domain.recruitment.dto.ApplicationRequest;
import com.donggle.domain.recruitment.dto.ApplicationResponse;
import com.donggle.domain.recruitment.dto.ApplicationStatusRequest;
import com.donggle.domain.recruitment.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController implements ApplicationApi {

    private final ApplicationService applicationService;

    @Override
    public ResponseEntity<ApplicationResponse> createApplication(
            Long recruitmentId, ApplicationRequest request, Long userId) {

        ApplicationResponse response =
                applicationService.createApplication(recruitmentId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<ApplicationResponse> updateApplication(
            Long applicationId, ApplicationRequest request, Long userId) {

        ApplicationResponse response =
                applicationService.updateApplication(applicationId, request, userId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> cancelApplication(Long applicationId, Long userId) {

        applicationService.cancelApplication(applicationId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            Long applicationId, ApplicationStatusRequest request, Long userId) {

        ApplicationResponse response =
                applicationService.updateApplicationStatus(applicationId, request, userId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApplicationResponse> getApplication(Long applicationId, Long userId) {

        ApplicationResponse response = applicationService.getApplication(applicationId, userId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Page<ApplicationResponse>> getApplicationsByRecruitment(
            Long recruitmentId, Pageable pageable, Long userId) {

        Page<ApplicationResponse> response =
                applicationService.getApplicationsByRecruitment(recruitmentId, pageable, userId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Page<ApplicationResponse>> getApplicationsByClub(
            Long clubId, Pageable pageable, Long userId) {

        Page<ApplicationResponse> response =
                applicationService.getApplicationsByClub(clubId, pageable, userId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Page<ApplicationResponse>> getMyApplications(
            Pageable pageable, Long userId) {

        Page<ApplicationResponse> response = applicationService.getMyApplications(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Page<ApplicationResponse>> getMyApplicationsByStatus(
            Application.ApplicationStatus status, Pageable pageable, Long userId) {

        Page<ApplicationResponse> response =
                applicationService.getMyApplicationsByStatus(userId, status, pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Long> countApplicationsByRecruitmentAndStatus(
            Long recruitmentId, Application.ApplicationStatus status) {

        long count =
                applicationService.countApplicationsByRecruitmentAndStatus(recruitmentId, status);
        return ResponseEntity.ok(count);
    }
}
