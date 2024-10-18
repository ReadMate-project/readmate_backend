package com.readmate.ReadMate.bookclub.repository;

import com.readmate.ReadMate.bookclub.entity.BookClub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookClubRepository extends JpaRepository<BookClub,Long>, JpaSpecificationExecutor<BookClub> {

    boolean existsByBookClubName(String clubName);

    List<BookClub> findAllByDelYn(String delYn);

    List<BookClub> findAllByOrderByViewCountDesc();

}
