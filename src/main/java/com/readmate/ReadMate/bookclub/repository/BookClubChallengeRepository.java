package com.readmate.ReadMate.bookclub.repository;

import com.readmate.ReadMate.bookclub.entity.BookClub;
import com.readmate.ReadMate.bookclub.entity.BookClubChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface BookClubChallengeRepository extends JpaRepository<BookClubChallenge,Long> {



    @Query("SELECT c FROM BookClubChallenge c WHERE c.bookClub.bookClubId = :bookClubId AND :date BETWEEN c.startDate AND c.endDate AND c.delYn = 'N'")
    BookClubChallenge findCurrentChallengeByBookClubIdAndDate(@Param("bookClubId") Long bookClubId, @Param("date") LocalDate date);

    List<BookClubChallenge>  findAllByBookClub(BookClub bookClub);

    List<BookClubChallenge> findAllByDelYnAndBookClub(String delYn, BookClub bookClub);

    List<BookClubChallenge> findAllByDelYn(String delYn);
}
