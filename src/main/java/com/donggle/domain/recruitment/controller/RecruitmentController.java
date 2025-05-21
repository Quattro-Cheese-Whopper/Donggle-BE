package com.donggle.domain.recruitment.controller;

import com.donggle.domain.club.domain.Club;
import com.donggle.domain.recruitment.domain.Recruitment;
import com.donggle.domain.recruitment.dto.RecruitmentRequest;
import com.donggle.domain.recruitment.dto.RecruitmentResponse;
import com.donggle.domain.recruitment.service.RecruitmentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recruitments")
@RequiredArgsConstructor
public class RecruitmentController implements RecruitmentApi {

    private final RecruitmentService recruitmentService;

    @Override
    public ResponseEntity<RecruitmentResponse> createRecruitment(
            Long clubId, RecruitmentRequest request, Long userId) {

        RecruitmentResponse response =
                recruitmentService.createRecruitment(clubId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<RecruitmentResponse> updateRecruitment(
            Long recruitmentId, RecruitmentRequest request, Long userId) {

        RecruitmentResponse response =
                recruitmentService.updateRecruitment(recruitmentId, request, userId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteRecruitment(Long recruitmentId, Long userId) {

        recruitmentService.deleteRecruitment(recruitmentId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<RecruitmentResponse> getRecruitment(Long recruitmentId) {
        RecruitmentResponse response = recruitmentService.getRecruitment(recruitmentId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<RecruitmentResponse>> getAllRecruitments() {
        List<RecruitmentResponse> response = recruitmentService.getAllRecruitments();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<RecruitmentResponse>> getActiveRecruitments() {
        List<RecruitmentResponse> response = recruitmentService.getActiveRecruitments();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<RecruitmentResponse>> getRecruitmentsByClub(Long clubId) {
        List<RecruitmentResponse> response = recruitmentService.getRecruitmentsByClub(clubId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<RecruitmentResponse>> getRecruitmentsByStatus(
            Recruitment.RecruitmentStatus status) {

        List<RecruitmentResponse> response = recruitmentService.getRecruitmentsByStatus(status);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<RecruitmentResponse>> getRecruitmentsByClubType(
            Club.ClubType type, Recruitment.RecruitmentStatus status) {

        List<RecruitmentResponse> response =
                recruitmentService.getRecruitmentsByClubType(type, status);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<RecruitmentResponse>> getRecruitmentsByClubCategory(
            Club.ClubCategory category, Recruitment.RecruitmentStatus status) {

        List<RecruitmentResponse> response =
                recruitmentService.getRecruitmentsByClubCategory(category, status);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> updateRecruitmentStatus(
            Long recruitmentId, Recruitment.RecruitmentStatus status, Long userId) {

        recruitmentService.updateRecruitmentStatus(recruitmentId, status, userId);
        return ResponseEntity.ok().build();
    }
}
