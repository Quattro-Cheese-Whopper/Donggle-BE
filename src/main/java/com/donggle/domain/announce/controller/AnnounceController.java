package com.donggle.domain.announce.controller;

import com.donggle.domain.announce.domain.Announce;
import com.donggle.domain.announce.dto.AnnounceRequest;
import com.donggle.domain.announce.dto.AnnounceResponse;
import com.donggle.domain.announce.service.AnnounceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/announces")
@RequiredArgsConstructor
public class AnnounceController implements AnnounceApi {

    private final AnnounceService announceService;

    @Override
    public ResponseEntity<AnnounceResponse> createGeneralAnnounce(
            AnnounceRequest request, Long userId) {

        AnnounceResponse response = announceService.createAnnounce(request, userId, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<AnnounceResponse> createClubAnnounce(
            Long clubId, AnnounceRequest request, Long userId) {

        AnnounceResponse response = announceService.createAnnounce(request, userId, clubId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<AnnounceResponse> updateAnnounce(
            Long announceId, AnnounceRequest request, Long userId) {

        AnnounceResponse response = announceService.updateAnnounce(announceId, request, userId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteAnnounce(Long announceId, Long userId) {

        announceService.deleteAnnounce(announceId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<AnnounceResponse> getAnnounce(Long announceId) {
        AnnounceResponse response = announceService.getAnnounce(announceId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Page<AnnounceResponse>> getAnnouncesByType(
            Announce.AnnounceType type, Pageable pageable) {

        Page<AnnounceResponse> response = announceService.getAnnouncesByType(type, pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Page<AnnounceResponse>> getAnnouncesByClub(
            Long clubId, Pageable pageable) {

        Page<AnnounceResponse> response = announceService.getAnnouncesByClub(clubId, pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<AnnounceResponse>> getRecentAnnouncesByType(
            Announce.AnnounceType type) {
        List<AnnounceResponse> response = announceService.getRecentAnnouncesByType(type);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<AnnounceResponse>> getRecentAnnouncesByClub(Long clubId) {
        List<AnnounceResponse> response = announceService.getRecentAnnouncesByClub(clubId);
        return ResponseEntity.ok(response);
    }
}
