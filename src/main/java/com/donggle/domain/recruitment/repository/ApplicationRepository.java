package com.donggle.domain.recruitment.repository;

import com.donggle.domain.recruitment.domain.Application;
import com.donggle.domain.recruitment.domain.Recruitment;
import com.donggle.domain.user.domain.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Page<Application> findByRecruitment(Recruitment recruitment, Pageable pageable);

    Page<Application> findByUser(User user, Pageable pageable);

    Page<Application> findByRecruitmentAndStatus(
            Recruitment recruitment, Application.ApplicationStatus status, Pageable pageable);

    Page<Application> findByUserAndStatus(
            User user, Application.ApplicationStatus status, Pageable pageable);

    Optional<Application> findByRecruitmentAndUser(Recruitment recruitment, User user);

    boolean existsByRecruitmentAndUser(Recruitment recruitment, User user);

    @Query(
            "SELECT COUNT(a) FROM Application a WHERE a.recruitment = :recruitment AND a.status = :status")
    long countByRecruitmentAndStatus(
            @Param("recruitment") Recruitment recruitment,
            @Param("status") Application.ApplicationStatus status);

    @Query("SELECT a FROM Application a WHERE a.recruitment.club.id = :clubId")
    Page<Application> findByClubId(@Param("clubId") Long clubId, Pageable pageable);
}
