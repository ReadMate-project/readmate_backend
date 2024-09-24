package com.readmate.ReadMate.bookclub.repository;

import com.readmate.ReadMate.bookclub.entity.BookClubMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookClubMemberRepository extends JpaRepository<BookClubMember, Long> {
    boolean existsByUserIdAndBookClubId(Long userId, Long bookClubId);

    BookClubMember findByUserIdAndBookClubId(Long userId, Long bookClubId);

    List<BookClubMember> findByBookClubId(Long bookClubId);
}

