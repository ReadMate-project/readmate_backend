package com.readmate.ReadMate.bookclub.service;

import com.readmate.ReadMate.bookclub.dto.req.BookClubJoinRequest;
import com.readmate.ReadMate.bookclub.dto.res.BookClubMemberResponse;
import com.readmate.ReadMate.bookclub.entity.BookClub;
import com.readmate.ReadMate.bookclub.entity.BookClubMember;
import com.readmate.ReadMate.bookclub.entity.BookClubMemberRole;
import com.readmate.ReadMate.bookclub.repository.BookClubMemberRepository;
import com.readmate.ReadMate.bookclub.repository.BookClubRepository;
import com.readmate.ReadMate.common.exception.CustomException;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookClubMemberService {
    private final BookClubMemberRepository bookClubMemberRepository;
    private final BookClubRepository bookClubRepository;

    public String joinClub(Long bookClubId, BookClubJoinRequest bookClubJoinRequest, CustomUserDetails userDetails) {

        // BookClubId가 존재하지 않을 경우 에러 발생
        if (bookClubRepository.existsById(bookClubId)) {
            // 이미 가입한 멤버일 경우 - 재가입 또는 에러코드
            if (bookClubMemberRepository.existsByUserIdAndBookClubId(userDetails.getUser().getUserId(), bookClubId)) {
                BookClubMember bookClubMember = bookClubMemberRepository.findByUserIdAndBookClubId(userDetails.getUser().getUserId(), bookClubId);
                if (bookClubMember.getDelYn().equals("N")){
                    throw new CustomException(ErrorCode.ALREADY_JOINED);
                }else{
                    bookClubMember.setDelYn("N");
                    bookClubMemberRepository.save(bookClubMember);
                }
            }else {

                // 가입 신청을 위한 새로운 멤버 생성
                BookClubMember bookClubMember = BookClubMember.builder()
                        .userId(userDetails.getUser().getUserId())
                        .clubMemberRole(BookClubMemberRole.MEMBER)
                        .bookClubId(bookClubId)
                        .joinMessage(bookClubJoinRequest.getJoinMessage())
                        .isApprove(false)
                        .build();
                bookClubMemberRepository.save(bookClubMember);
            }

            return "가입 신청이 완료되었습니다. 승인을 기다려주세요.";
        } else {
            throw new CustomException(ErrorCode.INVALID_CLUB);
        }
    }


    public String leaveClub(Long bookClubId, CustomUserDetails userDetails) {
        // BookClubId가 존재하지 않을 경우 에러 발생
        if (!bookClubRepository.existsById(bookClubId)) {
            throw new CustomException(ErrorCode.INVALID_CLUB);
        }

        //탈퇴 로직
        BookClubMember bookClubMember = bookClubMemberRepository.findByUserIdAndBookClubId(userDetails.getUser().getUserId(), bookClubId);
        if (bookClubMember != null) {
            //탈퇴하려는 USER가 현재 리더일 경우
            if(bookClubMember.getClubMemberRole().equals(BookClubMemberRole.LEADER)){
                throw new CustomException(ErrorCode.INVALID_LEAVE);
            }else {
                bookClubMember.setDelYn("Y");
                bookClubMember.setIsApprove(false);
                bookClubMemberRepository.save(bookClubMember);
                return "탈퇴가 완료되었습니다.";
            }
        } else {
            throw new CustomException(ErrorCode.NOT_MEMBER);
        }
    }

    public String approveMember(Long bookClubId, Long userId, CustomUserDetails userDetails) {
        //존재하지 않는 북클럽일 경우
        if (!bookClubRepository.existsById(bookClubId)) {
            throw new CustomException(ErrorCode.INVALID_CLUB);
        }
        //가입 신청이 안된 유저 일경우
        if (!bookClubMemberRepository.existsByUserIdAndBookClubId(userId,bookClubId)){
            throw new CustomException(ErrorCode.NOT_MEMBER);
        }
        //이미 가입된 유저일 경우
        BookClubMember bookClubMember = bookClubMemberRepository.findByUserIdAndBookClubId(userId,bookClubId);
        if (bookClubMember.getIsApprove() == Boolean.TRUE) {
            throw new CustomException(ErrorCode.ALREADY_JOINED);
        }else{
            bookClubMember.setIsApprove(Boolean.TRUE);
            bookClubMemberRepository.save(bookClubMember);
            return "북클럽 가입이 완료되었습니다.";
        }

    }

    public List<BookClubMemberResponse> findMember(Long bookClubId, CustomUserDetails userDetails) {
        //존재하지 않는 북클럽일 경우
        if (!bookClubRepository.existsById(bookClubId)) {
            throw new CustomException(ErrorCode.INVALID_CLUB);
        }
        //가입 신청이 안된 유저 일경우
        if (!bookClubMemberRepository.existsByUserIdAndBookClubId(userDetails.getUser().getUserId(), bookClubId)) {
            throw new CustomException(ErrorCode.NOT_MEMBER);
        }
        List<BookClubMember> bookClubMemberList = bookClubMemberRepository.findByBookClubId(bookClubId);
        List<BookClubMemberResponse> bookClubMemberResponses = bookClubMemberList.stream()
                .map(member -> new BookClubMemberResponse(
                        member.getBookClubMemberId(),
                        member.getUserId(),
                        member.getClubMemberRole(),
                        member.getBookClubId(),
                        member.getIsApprove(),
                        member.getJoinMessage(),
                        member.getDelYn()
                ))
                .collect(Collectors.toList());

        return bookClubMemberResponses;
    }
}
