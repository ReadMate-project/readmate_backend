package com.readmate.ReadMate.bookclub.repository;


import com.readmate.ReadMate.bookclub.entity.BookClubBook;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BookClubBookRepository extends JpaRepository<BookClubBook,Long> {


    @Query("SELECT b FROM BookClubBook b WHERE b.bookClub.bookClubId = :bookClubId")
    List<BookClubBook> findAllByBookClubId(@Param("bookClubId") Long bookClubId);

    // Custom query to fetch all books by clubId and isActive
    @Query("SELECT b FROM BookClubBook b WHERE b.bookClub.bookClubId = :bookClubId AND b.isActive = :isActive")
    List<BookClubBook> findAllByBookClubIdAndIsActive(@Param("bookClubId") Long bookClubId, @Param("isActive") boolean isActive);


}
