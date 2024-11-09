package com.readmate.ReadMate.bookclub.bookClubMember.repository;

import com.readmate.ReadMate.bookclub.bookClubMember.entity.BookClubMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface BookClubMemberRepository extends JpaRepository<BookClubMember, Long> {

    // 기본 회원 존재 여부 확인 - 승인된 회원 + 탈퇴하지 않은 상태
    boolean existsByUserIdAndBookClubIdAndIsApproveAndDelYnFalse(Long userId, Long bookClubId, boolean isApprove);

    // 기본 회원 조회 - userId와 bookClubId로 조회
    Optional<BookClubMember> findByUserIdAndBookClubId(Long userId, Long bookClubId);

    Optional<BookClubMember> findByUserIdAndBookClubIdAndIsApproveAndDelYn(Long userId, Long bookClubId, boolean isApprove, boolean delYn);
    // 회원 조회 - 승인 및 탈퇴 여부 조건으로 필터링 가능
    List<BookClubMember> findByBookClubIdAndIsApproveAndDelYn(long bookClubId, boolean isApprove, boolean delYn);

    // 특정 북클럽 내 탈퇴하지 않은 회원 조회
    List<BookClubMember> findByBookClubIdAndDelYnFalse(long bookClubId);

    List<BookClubMember> findByUserIdAndIsApproveAndDelYn(Long userId, boolean isApprove, boolean delYn);
}
