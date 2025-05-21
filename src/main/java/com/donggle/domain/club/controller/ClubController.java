package com.donggle.domain.club.controller;

import com.donggle.domain.club.domain.Club;
import com.donggle.domain.club.dto.ClubRequest;
import com.donggle.domain.club.dto.ClubResponse;
import com.donggle.domain.club.service.ClubService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class ClubController implements ClubApi {

    private final ClubService clubService;

    @Override
    public ResponseEntity<ClubResponse> createClub(ClubRequest request, Long userId) {
        ClubResponse response = clubService.createClub(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<ClubResponse> updateClub(Long clubId, ClubRequest request, Long userId) {
        ClubResponse response = clubService.updateClub(clubId, request, userId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteClub(Long clubId, Long userId) {
        clubService.deleteClub(clubId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ClubResponse> getClub(Long clubId) {
        ClubResponse response = clubService.getClub(clubId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<ClubResponse>> getAllClubs() {
        List<ClubResponse> response = clubService.getAllClubs();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<ClubResponse>> getClubsByType(Club.ClubType type) {
        List<ClubResponse> response = clubService.getClubsByType(type);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<ClubResponse>> getClubsByCategory(Club.ClubCategory category) {
        List<ClubResponse> response = clubService.getClubsByCategory(category);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Page<ClubResponse>> getClubsByTypeAndCategory(
            Club.ClubType type, Club.ClubCategory category, Pageable pageable) {
        Page<ClubResponse> response =
                clubService.getClubsByTypeAndCategory(type, category, pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Page<ClubResponse>> searchClubs(String keyword, Pageable pageable) {
        Page<ClubResponse> response = clubService.searchClubs(keyword, pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<ClubResponse>> getMyClubs(Long userId) {
        List<ClubResponse> response = clubService.getClubsByManager(userId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> addManager(Long clubId, Long managerId, Long userId) {
        clubService.addManager(clubId, managerId, userId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> removeManager(Long clubId, Long managerId, Long userId) {
        clubService.removeManager(clubId, managerId, userId);
        return ResponseEntity.noContent().build();
    }
}
