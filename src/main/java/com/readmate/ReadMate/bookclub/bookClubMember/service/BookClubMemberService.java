package com.readmate.ReadMate.bookclub.bookClubMember.service;

import com.readmate.ReadMate.bookclub.bookClubMember.dto.BookClubJoinRequest;
import com.readmate.ReadMate.bookclub.bookClubMember.dto.BookClubMemberResponse;
import com.readmate.ReadMate.bookclub.bookClubMember.entity.BookClubMember;
import com.readmate.ReadMate.bookclub.bookClubMember.entity.BookClubMemberRole;
import com.readmate.ReadMate.bookclub.bookClubMember.repository.BookClubMemberRepository;

import com.readmate.ReadMate.bookclub.bookClubMember.validator.BookClubMemberValidator;
import com.readmate.ReadMate.bookclub.club.entity.BookClub;
import com.readmate.ReadMate.bookclub.club.validator.BookClubValidator;
import com.readmate.ReadMate.common.exception.CustomException;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookClubMemberService {
    private final BookClubMemberRepository bookClubMemberRepository;
    private final BookClubMemberValidator bookClubMemberValidator;
    private final BookClubValidator bookClubValidator;


    // BookClubMember -> BookClubMemberResponse 변환 공통 로직
    private BookClubMemberResponse toResponse(BookClubMember member) {
        return new BookClubMemberResponse(
                member.getBookClubMemberId(),
                member.getUserId(),
                member.getClubMemberRole(),
                member.getBookClubId(),
                member.getIsApprove(),
                member.getJoinMessage(),
                member.isDelYn()
        );
    }

    // 북클럽 가입 메서드
    public String joinClub(final long bookClubId, final BookClubJoinRequest request, final long userId) {
        bookClubValidator.validateBookClubExists(bookClubId);
        bookClubMemberValidator.validateMemberAlreadyJoined(bookClubId, userId);


        BookClubMember bookClubMember = bookClubMemberRepository.findByUserIdAndBookClubId(userId, bookClubId)
                .orElse(null);

        if (bookClubMember != null) {
            if (!bookClubMember.isDelYn()) { // 탈퇴한 유저가 아니면
                throw new CustomException(ErrorCode.ALREADY_JOINED);
            } //탈퇴했던 유저면 delYn False
            bookClubMember.setDelYn(false);
        } else {
            bookClubMember = BookClubMember.builder()
                    .userId(userId)
                    .clubMemberRole(BookClubMemberRole.MEMBER)
                    .bookClubId(bookClubId)
                    .joinMessage(request.getJoinMessage())
                    .isApprove(false)
                    .build();
        }
        bookClubMemberRepository.save(bookClubMember);
        return "가입 신청이 완료되었습니다. 승인을 기다려주세요.";
    }

    // 북클럽 탈퇴 메서드
    public String leaveClub(final long bookClubId, final long userId) {
        bookClubValidator.validateBookClubExists(bookClubId);
        BookClubMember bookClubMember = bookClubMemberRepository.findByUserIdAndBookClubId(userId, bookClubId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_MEMBER));

        if (bookClubMember.getClubMemberRole().equals(BookClubMemberRole.LEADER)) {
            throw new CustomException(ErrorCode.INVALID_LEAVE);
        }
        bookClubMember.setDelYn(true);
        bookClubMember.setIsApprove(false);
        bookClubMemberRepository.save(bookClubMember);
        return "탈퇴가 완료되었습니다.";
    }

    // 멤버 승인 메서드
    public String approveMember(final long bookClubId, final long userId) {
        bookClubValidator.validateBookClubExists(bookClubId);

        BookClubMember bookClubMember = bookClubMemberRepository.findByUserIdAndBookClubId(userId, bookClubId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_MEMBER));

        if (bookClubMember.getIsApprove()) {
            throw new CustomException(ErrorCode.ALREADY_JOINED);
        }
        bookClubMember.setIsApprove(true);
        bookClubMemberRepository.save(bookClubMember);
        return "북클럽 가입이 완료되었습니다.";
    }

    // 가입된 멤버 조회 (필터 적용 가능)
    // includePending이 true일 경우 탈퇴하지 않은 모든 회원을 조회하고, false일 경우 승인된 회원만 조회합니다.
    public List<BookClubMemberResponse> findMembers(final long bookClubId,final long userId, boolean includePending) {
        bookClubValidator.validateBookClubExists(bookClubId);
        bookClubMemberValidator.validateApprovedMember(bookClubId, userId);

        List<BookClubMember> members = includePending
                ? bookClubMemberRepository.findByBookClubIdAndDelYnFalse(bookClubId) // 승인 여부와 상관없이 탈퇴하지 않은 모든 멤버
                : bookClubMemberRepository.findByBookClubIdAndIsApproveAndDelYn(bookClubId, true, false); // 승인된 멤버만

        return members.stream().map(this::toResponse).collect(Collectors.toList());
    }


    // 유저가 해당 북클럽의 승인된 멤버인지 확인
    // 승인되지 않은 회원이거나 탈퇴한 회원일 경우 예외
    public void validateApprovedMember(final long bookClubId, final long userId) {
        bookClubValidator.validateBookClubExists(bookClubId);

        boolean isApproved = bookClubMemberRepository.existsByUserIdAndBookClubIdAndIsApproveAndDelYnFalse(userId, bookClubId,true);
        if (!isApproved) {
            throw new CustomException(ErrorCode.NOT_APPROVED_MEMBER);
        }
    }

    // 특정 유저가 가입된 모든 북클럽 조회
    public List<Long> findBookClubIdsByUserId(Long userId) {
        List<BookClubMember> members = bookClubMemberRepository.findByUserIdAndIsApproveAndDelYn(userId, true, false);
        return members.stream().map(BookClubMember::getBookClubId).collect(Collectors.toList());
    }


    // 승인된 멤버 조회
    public BookClubMember findApprovedMember(final long bookClubId, final long userId) {
        bookClubValidator.validateBookClubExists(bookClubId);

        return bookClubMemberRepository.findByUserIdAndBookClubIdAndIsApproveAndDelYn(userId, bookClubId,true,false)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_MEMBER));
    }

    // 리더 가입 처리
    public void joinLeader(Long bookClubId, Long leaderId) {
        BookClubMember leader = BookClubMember.builder()
                .userId(leaderId)
                .clubMemberRole(BookClubMemberRole.LEADER)
                .bookClubId(bookClubId)
                .joinMessage("북클럽 리더 입니다")
                .isApprove(true)
                .build();
        bookClubMemberRepository.save(leader);
    }

    // 리더 변경
    public void changeLeader(BookClub bookClub, Long newLeaderId) {
        BookClubMember newLeader = bookClubMemberRepository.findByUserIdAndBookClubId(newLeaderId, bookClub.getBookClubId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_MEMBER));

        BookClubMember oldLeader = bookClubMemberRepository.findByUserIdAndBookClubId(bookClub.getLeaderId(), bookClub.getBookClubId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CLUB_MEMBER));

        if (!newLeader.getIsApprove() || newLeader.isDelYn()) {
            throw new CustomException(ErrorCode.NOT_APPROVED_MEMBER);
        }

        bookClub.setLeaderId(newLeaderId);
        newLeader.setClubMemberRole(BookClubMemberRole.LEADER);
        oldLeader.setClubMemberRole(BookClubMemberRole.MEMBER);

        bookClubMemberRepository.save(newLeader);
        bookClubMemberRepository.save(oldLeader);
    }
    //북클럽 Id로 회원수 조회
    public int getMemberCountByBookClubId(Long bookClubId) {
        return bookClubMemberRepository.countByBookClubId(bookClubId);
    }
}
