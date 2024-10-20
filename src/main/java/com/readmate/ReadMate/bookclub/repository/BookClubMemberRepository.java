package com.readmate.ReadMate.bookclub.repository;

import com.readmate.ReadMate.bookclub.entity.BookClub;
import com.readmate.ReadMate.bookclub.entity.BookClubMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookClubMemberRepository extends JpaRepository<BookClubMember, Long> {
    boolean existsByUserIdAndBookClub(Long userId, BookClub bookClub);

    BookClubMember findByUserIdAndBookClub(Long userId, BookClub bookClub);


    List<BookClubMember> findByBookClub(BookClub bookClub);

    // In BookClubMemberRepository
    List<BookClubMember> findAllByDelYnAndUserId(String delYn, Long userId);

    @Query("SELECT bcm.bookClub.bookClubId FROM BookClubMember bcm WHERE bcm.userId = :userId")
    List<Long> findBookClubIdsByUserId(@Param("userId") Long userId);



    @Query("SELECT m.bookClub FROM BookClubMember m WHERE m.userId = :userId AND m.delYn = 'N' AND m.isApprove = True")
    List<BookClub> findAllBookClubsByUserId(@Param("userId") Long userId);

    // 탈퇴하지 않았고 가입이 승인된 멤버 조회
    List<BookClubMember> findByBookClubAndIsApproveAndDelYn(BookClub bookClub, Boolean isApprove, String delYn);

    boolean existsByUserIdAndBookClubAndIsApproveAndDelYn(Long userId, BookClub bookClub, boolean b, String n);

    Optional<BookClubMember> findByUserIdAndBookClubAndIsApproveAndDelYn(Long userId, BookClub bookClub, boolean isApprove, String delYn);
}

