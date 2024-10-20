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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookClubMemberService {
    private final BookClubMemberRepository bookClubMemberRepository;
    private final BookClubRepository bookClubRepository;

    public String joinClub(Long bookClubId, BookClubJoinRequest bookClubJoinRequest, CustomUserDetails userDetails) {

        BookClub bookClub = bookClubRepository.findById(bookClubId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CLUB));

        // BookClubId가 존재하지 않을 경우 에러 발생

            // 이미 가입한 멤버일 경우 - 재가입 또는 에러코드
            if (bookClubMemberRepository.existsByUserIdAndBookClub(userDetails.getUser().getUserId(), bookClub)) {
                BookClubMember bookClubMember = bookClubMemberRepository.findByUserIdAndBookClub(userDetails.getUser().getUserId(), bookClub);
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
                        .bookClub(bookClub)
                        .joinMessage(bookClubJoinRequest.getJoinMessage())
                        .isApprove(false)
                        .build();
                bookClubMemberRepository.save(bookClubMember);
            }

            return "가입 신청이 완료되었습니다. 승인을 기다려주세요.";
        }



    public String leaveClub(Long bookClubId, CustomUserDetails userDetails) {

        BookClub bookClub = bookClubRepository.findById(bookClubId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CLUB));


        //탈퇴 로직
        BookClubMember bookClubMember = bookClubMemberRepository.findByUserIdAndBookClub(userDetails.getUser().getUserId(), bookClub);
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
        BookClub bookClub = bookClubRepository.findById(bookClubId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CLUB));

        //가입 신청이 안된 유저 일경우
        if (!bookClubMemberRepository.existsByUserIdAndBookClub(userId,bookClub)){
            throw new CustomException(ErrorCode.NOT_MEMBER);
        }
        //이미 가입된 유저일 경우
        BookClubMember bookClubMember = bookClubMemberRepository.findByUserIdAndBookClub(userId,bookClub);
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
        BookClub bookClub = bookClubRepository.findById(bookClubId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CLUB));

        //가입 신청이 안된 유저 일경우
        if (!bookClubMemberRepository.existsByUserIdAndBookClub(userDetails.getUser().getUserId(), bookClub)) {
            throw new CustomException(ErrorCode.NOT_MEMBER);
        }
        List<BookClubMember> bookClubMemberList = bookClubMemberRepository.findByBookClub(bookClub);
        List<BookClubMemberResponse> bookClubMemberResponses = bookClubMemberList.stream()
                .map(member -> new BookClubMemberResponse(
                        member.getBookClubMemberId(),
                        member.getUserId(),
                        member.getClubMemberRole(),
                        member.getBookClub(),
                        member.getIsApprove(),
                        member.getJoinMessage(),
                        member.getDelYn())

                )
                .collect(Collectors.toList());

        return bookClubMemberResponses;
    }

    /**
     * 탈퇴하지 않고 가입 승인된 현재 북클럽 멤버 조회
     * @param bookClubId
     * @return List<BookClubMemberResponse>
     */
    public List<BookClubMemberResponse> findApprovedMembers(Long bookClubId) {
        // 존재하지 않는 북클럽일 경우 예외 발생
        BookClub bookClub = bookClubRepository.findById(bookClubId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CLUB));

        // 탈퇴하지 않은 승인된 멤버 목록 조회
        List<BookClubMember> approvedMembers = bookClubMemberRepository.findByBookClubAndIsApproveAndDelYn(bookClub, true, "N");

        // BookClubMemberResponse 형태로 변환하여 반환
        return approvedMembers.stream()
                .map(member -> new BookClubMemberResponse(
                        member.getBookClubMemberId(),
                        member.getUserId(),
                        member.getClubMemberRole(),
                        member.getBookClub(),
                        member.getIsApprove(),
                        member.getJoinMessage(),
                        member.getDelYn()))
                .collect(Collectors.toList());
    }


    /**
     * 유저가 특정 북클럽의 승인된 멤버인지 확인하는 메서드
     * @param bookClubId
     * @param userId
     * @return boolean (유저가 승인된 멤버인지 여부)
     */

    public boolean isUserApprovedMember(Long bookClubId, Long userId) {
        // 로그 추가
        log.info("Checking if user {} is an approved member of book club {}", userId, bookClubId);

        // 북클럽이 존재하는지 확인
        BookClub bookClub = bookClubRepository.findById(bookClubId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CLUB));

        // 로그 추가
        log.info("Found book club: {}", bookClub.getBookClubName());

        // 유저가 해당 북클럽의 승인된 멤버인지 확인
        boolean isApprovedMember = bookClubMemberRepository.existsByUserIdAndBookClubAndIsApproveAndDelYn(userId, bookClub, true, "N");

        // 로그 추가
        log.info("Is user approved: {}", isApprovedMember);

        return isApprovedMember;
    }

    /**
     * 특정 유저가 북클럽의 승인된 멤버인지 확인하고, 해당 멤버 객체를 반환하는 메서드
     * @param bookClubId
     * @param userId
     * @return BookClubMember
     */
    public BookClubMember findApprovedMemberByUserId(Long bookClubId, Long userId) {
        // 존재하지 않는 북클럽일 경우 예외 처리
        BookClub bookClub = bookClubRepository.findById(bookClubId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CLUB));

        // 유저가 해당 북클럽의 승인된 멤버인지 확인하고, 없을 경우 예외 처리
        return bookClubMemberRepository.findByUserIdAndBookClubAndIsApproveAndDelYn(userId, bookClub, true, "N")
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_MEMBER));
    }




}
