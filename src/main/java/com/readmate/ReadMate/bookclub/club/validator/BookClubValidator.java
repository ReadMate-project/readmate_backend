package com.readmate.ReadMate.bookclub.club.validator;

import com.readmate.ReadMate.bookclub.club.dto.req.BookClubRequest;
import com.readmate.ReadMate.bookclub.club.repository.BookClubRepository;
import com.readmate.ReadMate.common.exception.CustomException;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@AllArgsConstructor
public class BookClubValidator {

    private final BookClubRepository bookClubRepository;

    public static void validate(BookClubRequest clubRequest) {
        validateRecruitmentAndProgressPeriods(clubRequest);
    }


    private static void validateRecruitmentAndProgressPeriods(BookClubRequest clubRequest) {
        LocalDate recruitmentStart = clubRequest.getRecruitmentStartDate();
        LocalDate recruitmentEnd = clubRequest.getRecruitmentEndDate();
        LocalDate progressStart = clubRequest.getStartDate();
        LocalDate progressEnd = clubRequest.getEndDate();

        // 모집 종료일이 모집 시작일보다 앞설 경우 에러
        // 북클럽 시작일이 모집 마감일 전일 경우
        // 북클럽 진행 마감날이 시작날 전일 경우
        if (recruitmentEnd.isBefore(recruitmentStart) || progressStart.isBefore(recruitmentEnd) || progressEnd.isBefore(progressStart)) {
            throw new CustomException(ErrorCode.INVALID_CLUB_DATES);
        }

    }


    // 북클럽 ID 유효성 검증
    public void validateBookClubExists(final long bookClubId) {
        if (!bookClubRepository.existsByBookClubId(bookClubId)) {
            throw new CustomException(ErrorCode.INVALID_CLUB);
        }
    }

    public static void validateLeaderPermission(Long leaderId, Long userId) {
        if (!userId.equals(leaderId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }
    }

