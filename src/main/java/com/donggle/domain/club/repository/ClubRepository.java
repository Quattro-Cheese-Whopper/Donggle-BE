package com.donggle.domain.club.repository;

import com.donggle.domain.club.domain.Club;
import com.donggle.domain.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClubRepository extends JpaRepository<Club, Long> {

    Optional<Club> findByName(String name);

    List<Club> findByType(Club.ClubType type);

    List<Club> findByCategory(Club.ClubCategory category);

    Page<Club> findByTypeAndCategory(
            Club.ClubType type, Club.ClubCategory category, Pageable pageable);

    @Query("SELECT c FROM Club c WHERE c.name LIKE %:keyword% OR c.description LIKE %:keyword%")
    Page<Club> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM Club c JOIN c.managers m WHERE m = :manager")
    List<Club> findByManager(@Param("manager") User manager);

    boolean existsByName(String name);
}
