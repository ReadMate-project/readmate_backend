package com.readmate.ReadMate.bookclub.service;

import com.readmate.ReadMate.book.entity.Book;
import com.readmate.ReadMate.bookclub.dto.res.MissionResponse;
import com.readmate.ReadMate.bookclub.dto.res.UserMissionResponse;
import com.readmate.ReadMate.bookclub.entity.*;
import com.readmate.ReadMate.bookclub.repository.*;
import com.readmate.ReadMate.common.exception.CustomException;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookClubChallengeService {

    private final BookClubChallengeRepository bookClubChallengeRepository;
    private final DailyMissionRepository dailyMissionRepository;
    private final BookClubMemberRepository bookClubMemberRepository;
    private final BookClubRepository bookClubRepository;
    private final DailyMissionCompletionRepository dailyMissionCompletionRepository;
    @Transactional
    public MissionResponse getClubChallenge(CustomUserDetails userDetails, Long bookClubId) {

        //접속한 USER (UserDetail ) 가 회원이 아니면 에러 처리
        if (!isUserMemberOfClub(userDetails, bookClubId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        LocalDate today = LocalDate.now();

        BookClubChallenge currentChallenge = bookClubChallengeRepository
                .findCurrentChallengeByBookClubIdAndDate(bookClubId, today);

        //미션이 없을 경우 Return
        if (currentChallenge == null) {
            throw new CustomException(ErrorCode.NO_CHALLENGE_TODAY);
        }

        DailyMission dailyMission = dailyMissionRepository
                .findByChallengeAndMissionDate(currentChallenge, today);

        Book book = currentChallenge.getBook();
        int progressPercentage = (int) ((double) dailyMission.getPagesToRead() / book.getTotalPages() * 100);

        if (dailyMission == null) {
            throw new RuntimeException("No mission found for today.");
        }


        return MissionResponse.builder()
                .missionId(dailyMission.getMissionId())
                .date(dailyMission.getMissionDate())
                .todayPage(dailyMission.getPagesToRead())
                .title(currentChallenge.getBook().getTitle())
                .bookCover(currentChallenge.getBook().getBookCover())
                .progressPercentage(progressPercentage)
                .build();

    }


    private boolean isUserMemberOfClub(CustomUserDetails userDetails, Long bookClubId) {

        BookClub bookClub = bookClubRepository.findById(bookClubId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CLUB));


        BookClubMember bookClubMember = bookClubMemberRepository.findByUserIdAndBookClub(userDetails.getUser().getUserId(),bookClub);
       if(bookClubMember!=null && bookClubMember.getIsApprove()) {
           return true;
       }
       return false;
    }


    public void createClubChallenge(BookClub bookClub, Book book, LocalDate startDate, LocalDate endDate) {
        BookClubChallenge bookClubChallenge = new BookClubChallenge();
        bookClubChallenge.setBookClub(bookClub);
        bookClubChallenge.setStartDate(startDate);
        bookClubChallenge.setEndDate(endDate);
        bookClubChallenge.setBook(book); //DB에 저장한 ID
        BookClubChallenge savedBookClubChallenge =bookClubChallengeRepository.save(bookClubChallenge);

        createMissionsForChallenge(savedBookClubChallenge);
    }


    /**
     * 북클럽 챌린지 생성
     */
    public void createMissionsForChallenge(BookClubChallenge challenge) {


        LocalDate startDate = challenge.getStartDate();
        LocalDate endDate = challenge.getEndDate();
        Long totalPages = challenge.getBook().getTotalPages();
        int days = (int) (endDate.toEpochDay() - startDate.toEpochDay() + 1);

        // 하루에 읽어야 할 페이지 수
        int pagesPerDay = (int) (totalPages / days);
        // 나머지 페이지 수
        int remainder = (int) (totalPages % days);

        List<DailyMission> dailyMissions = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // 나머지 페이지가 남아있는 경우
            if (remainder > 0) {
                dailyMissions.add(DailyMission.builder()
                        .missionDate(date)
                        .pagesToRead(pagesPerDay + 1) // 하루에 1페이지 더 읽기
                        .challenge(challenge)
                        .build());
                remainder--; // 나머지 페이지 수 줄이기
            } else {
                dailyMissions.add(DailyMission.builder()
                        .missionDate(date)
                        .pagesToRead(pagesPerDay) // 일반적인 페이지 수
                        .challenge(challenge)
                        .build());
            }
        }

        dailyMissionRepository.saveAll(dailyMissions);
    }


    public List<UserMissionResponse> getUserChallenge(CustomUserDetails userDetails) {

        List<BookClubMember> bookClubMembers = bookClubMemberRepository.findAllByDelYnAndUserId("N", userDetails.getUser().getUserId());

        if(bookClubMembers.isEmpty()){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // BookClub 리스트 생성
         List<BookClub> bookClubs = bookClubMembers.stream()
                .map(BookClubMember::getBookClub)
                .collect(Collectors.toList());

        // 받아온 BookClubID를 사용해서 유저가 참여한 북클럽이 진행 중인 BookClubChallenge 리스트를 받아옴
        List<BookClubChallenge> bookClubChallenges = new ArrayList<>();
        for (BookClub bookClub : bookClubs) {
            bookClubChallenges.addAll(bookClubChallengeRepository.findAllByDelYnAndBookClub("N",bookClub));
        }
            // 오늘 날짜
            LocalDate today = LocalDate.now();

            // 오늘 진행해야 하는 DailyMission 리스트를 받아오는 로직
            List<DailyMission> dailyMissionList = new ArrayList<>();
            for (BookClubChallenge challenge : bookClubChallenges) {
                List<DailyMission> missionsForToday = dailyMissionRepository.findAllByChallengeAndMissionDate(challenge, today);
                dailyMissionList.addAll(missionsForToday);
            }

            // UserMissionResponse 리스트로 변환하여 반환
            return dailyMissionList.stream()
                    .map(dailyMission -> UserMissionResponse.builder()
                            .missionId(dailyMission.getMissionId())
                            .challengeId(dailyMission.getMissionId())
                            .bookClubId(dailyMission.getChallenge().getBookClub().getBookClubId())
                            .date(dailyMission.getMissionDate())
                            .todayPage(dailyMission.getPagesToRead())
                            // 필요에 따라 책 제목과 표지 추가
                            .title(dailyMission.getChallenge().getBook().getTitle())
                            .bookCover(dailyMission.getChallenge().getBook().getBookCover())
                            .build())
                    .collect(Collectors.toList());
        }


    public List<BookClubMember> getCompletedMembers(Long dailyMissionId, LocalDate date) {
        return dailyMissionCompletionRepository
                .findAllByDailyMissionIdAndCompletionDate(dailyMissionId, date)
                .stream()
                .map(DailyMissionCompletion::getMember)
                .collect(Collectors.toList());
    }


    /**
     * 미션 완료 시 저장하는 메서드
     * @param dailyMissionId
     * @param memberId
     */
    public void completeMission(Long dailyMissionId, Long memberId) {
        DailyMission dailyMission = dailyMissionRepository.findById(dailyMissionId)
                .orElseThrow(() -> new RuntimeException("Mission not found"));

        BookClubMember member = bookClubMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        DailyMissionCompletion completion = DailyMissionCompletion.builder()
                .dailyMission(dailyMission)
                .member(member)
                .completionDate(LocalDate.now())
                .build();

        dailyMissionCompletionRepository.save(completion);
    }



}

