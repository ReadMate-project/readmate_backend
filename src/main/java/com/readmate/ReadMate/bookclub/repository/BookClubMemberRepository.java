package com.readmate.ReadMate.bookclub.repository;

import com.readmate.ReadMate.bookclub.entity.BookClub;
import com.readmate.ReadMate.bookclub.entity.BookClubMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookClubMemberRepository extends JpaRepository<BookClubMember, Long> {
    boolean existsByUserIdAndBookClub(Long userId, BookClub bookClub);

    BookClubMember findByUserIdAndBookClub(Long userId, BookClub bookClub);


    List<BookClubMember> findByBookClub(BookClub bookClub);

    List<BookClubMember> findAllByDelYnAndBookClub(String delYn, BookClub bookClub);


    // In BookClubMemberRepository
    List<BookClubMember> findAllByDelYnAndUserId(String delYn, Long userId);


}

