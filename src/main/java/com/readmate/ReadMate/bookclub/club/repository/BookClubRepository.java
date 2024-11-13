package com.readmate.ReadMate.bookclub.club.repository;

import com.readmate.ReadMate.bookclub.club.entity.BookClub;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookClubRepository extends JpaRepository<BookClub,Long>, JpaSpecificationExecutor<BookClub> {

    boolean existsByBookClubName(String clubName);

    boolean existsByBookClubId(final long bookClubId);

    Page<BookClub> findByBookClubIdIn(List<Long> bookIds, Pageable pageable);
}
