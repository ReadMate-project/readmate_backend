package com.readmate.ReadMate.bookclub.repository;

import com.readmate.ReadMate.bookclub.entity.BookClub;
import com.readmate.ReadMate.bookclub.entity.BookClubMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookClubRepository extends JpaRepository<BookClub,Long>, JpaSpecificationExecutor<BookClub> {

    boolean existsByBookClubName(String clubName);

    List<BookClub> findAllByDelYn(String delYn);

    @Query("SELECT m.bookClub FROM BookClubMember m WHERE m.userId = :userId")
    List<BookClub> findAllBookClubsByUserId(@Param("userId") Long userId);

    List<BookClub> findAllByOrderByViewCountDesc();

}
