package com.readmate.ReadMate.bookclub.bookClubChallenge.repository;

import com.readmate.ReadMate.bookclub.bookClubChallenge.entity.BookClubChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookClubChallengeRepository extends JpaRepository<BookClubChallenge,Long> {

    @Query("SELECT c FROM BookClubChallenge c WHERE c.bookClubId = :bookClubId AND c.delYn = false AND (c.startDate <= :date AND c.endDate >= :date)")
    Optional<BookClubChallenge> findCurrentChallengeByBookClubIdAndDate(@Param("bookClubId") Long bookClubId, @Param("date") LocalDate date);

    List<BookClubChallenge>  findAllByBookClubId(Long bookClubId);

    List<BookClubChallenge> findAllByDelYnAndBookClubId(boolean delYn, Long bookClubId);

    List<BookClubChallenge> findAllByDelYnFalse();

    List<BookClubChallenge>findByIsbn13(long isbn13);

}
