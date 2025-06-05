package com.donggle.domain.recruitment.repository;

import com.donggle.domain.club.domain.Club;
import com.donggle.domain.recruitment.domain.Recruitment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {

    List<Recruitment> findByClub(Club club);

    List<Recruitment> findByStatus(Recruitment.RecruitmentStatus status);

    // 동아리별 최신 모집공고 조회 (생성일 기준 내림차순)
    Optional<Recruitment> findTopByClubOrderByCreatedAtDesc(Club club);

    @Query(
            "SELECT r FROM Recruitment r WHERE r.startDate <= :now AND (r.endDate IS NULL OR r.endDate >= :now)")
    List<Recruitment> findActiveRecruitments(LocalDateTime now);

    @Query("SELECT r FROM Recruitment r WHERE r.club.type = :type AND r.status = :status")
    List<Recruitment> findByClubTypeAndStatus(
            Club.ClubType type, Recruitment.RecruitmentStatus status);

    @Query("SELECT r FROM Recruitment r WHERE r.club.category = :category AND r.status = :status")
    List<Recruitment> findByClubCategoryAndStatus(
            Club.ClubCategory category, Recruitment.RecruitmentStatus status);
}
