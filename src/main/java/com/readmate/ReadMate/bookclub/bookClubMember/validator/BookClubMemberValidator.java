package com.readmate.ReadMate.bookclub.bookClubMember.validator;


import com.readmate.ReadMate.bookclub.bookClubMember.repository.BookClubMemberRepository;
import com.readmate.ReadMate.bookclub.club.repository.BookClubRepository;
import com.readmate.ReadMate.common.exception.CustomException;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookClubMemberValidator {

    private final BookClubMemberRepository bookClubMemberRepository;


    // 북클럽의 특정 회원 승인 및 탈퇴 여부 확인
    public void validateApprovedMember(final long bookClubId, final long userId) {
        boolean isApproved = bookClubMemberRepository.existsByUserIdAndBookClubIdAndIsApproveAndDelYnFalse(userId, bookClubId,true);
        if (!isApproved) {
            throw new CustomException(ErrorCode.NOT_APPROVED_MEMBER);
        }
    }

    // 회원이 해당 북클럽에 이미 가입되어 있는지 확인
    public void validateMemberAlreadyJoined(final long bookClubId, final long userId) {
        boolean isMember = bookClubMemberRepository.existsByUserIdAndBookClubIdAndIsApproveAndDelYnFalse(userId, bookClubId,true);
        if (isMember) {
            throw new CustomException(ErrorCode.ALREADY_JOINED);
        }
    }
}