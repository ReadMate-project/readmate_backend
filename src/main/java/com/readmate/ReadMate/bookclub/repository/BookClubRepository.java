package com.readmate.ReadMate.bookclub.repository;

import com.readmate.ReadMate.bookclub.entity.BookClub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookClubRepository extends JpaRepository<BookClub,Long> {

    boolean existsByBookClubName(String clubName);
}
