package com.readmate.ReadMate.bookclub.club.repository;

import com.readmate.ReadMate.bookclub.club.entity.BookClub;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookClubRepository extends JpaRepository<BookClub,Long>, JpaSpecificationExecutor<BookClub> {

    boolean existsByBookClubName(String clubName);

    Page<BookClub> findAllByDelYnFalseOrderByCreatedAtDesc(Specification spec, Pageable pageable);

    boolean existsByBookClubId(final long bookClubId);
}
