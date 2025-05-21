package com.donggle.domain.announce.repository;

import com.donggle.domain.announce.domain.Announce;
import com.donggle.domain.club.domain.Club;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnounceRepository extends JpaRepository<Announce, Long> {

    Page<Announce> findByType(Announce.AnnounceType type, Pageable pageable);

    Page<Announce> findByClub(Club club, Pageable pageable);

    List<Announce> findTop5ByTypeOrderByCreatedAtDesc(Announce.AnnounceType type);

    List<Announce> findTop5ByClubOrderByCreatedAtDesc(Club club);
}
