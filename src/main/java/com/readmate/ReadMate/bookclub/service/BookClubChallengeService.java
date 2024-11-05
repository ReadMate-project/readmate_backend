package com.readmate.ReadMate.bookclub.service;

import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.book.entity.Book;
import com.readmate.ReadMate.book.repository.BookRepository;
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

    private  final BookClubChallengeRepository bookClubChallengeRepository;
    private  final DailyMissionRepository dailyMissionRepository;
    private final BookClubMemberRepository bookClubMemberRepository;
    private final BookClubRepository bookClubRepository;
    private final DailyMissionCompletionRepository dailyMissionCompletionRepository;
    private  final BookRepository bookRepository;

    @Transactional
    public MissionResponse getClubChallenge(CustomUserDetails userDetails, Long bookClubId) {

        BookClub bookClub = bookClubRepository.findById(bookClubId)
                        .orElseThrow(()-> new CustomException(ErrorCode.BOOK_NOT_FOUND));

        // 유저가 해당 북클럽의 승인된 멤버인지 확인
        BookClubMember member = bookClubMemberRepository.findByUserIdAndBookClubAndIsApproveAndDelYn(
                        userDetails.getUser().getUserId(), bookClub, true, "N")
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_MEMBER));

        // 접속한 USER가 회원이 아니면 에러 처리
        if (member==null) {
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
        int progressPercentage = (int) ((double) dailyMission.getEndPage() / book.getTotalPages() * 100);

        if (dailyMission == null) {
            throw new RuntimeException("No mission found for today.");
        }

        return MissionResponse.builder()
                .missionId(dailyMission.getMissionId())
                .date(dailyMission.getMissionDate())
                .startPage(dailyMission.getStartPage())
                .endPage(dailyMission.getEndPage())
                .title(currentChallenge.getBook().getTitle())
                .bookCover(currentChallenge.getBook().getBookCover())
                .progressPercentage(progressPercentage)
                .build();

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

        int currentPage = 1; // 책의 첫 페이지부터 시작

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            int endPage = currentPage + pagesPerDay - 1;

            // 나머지가 있는 날은 한 페이지 더 추가
            if (remainder > 0) {
                endPage += 1;
                remainder--;
            }

            dailyMissions.add(DailyMission.builder()
                    .missionDate(date)
                    .startPage(currentPage)
                    .endPage(endPage)
                    .challenge(challenge)
                    .build());

            currentPage = endPage + 1; // 다음 시작 페이지
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
                            .startPage(dailyMission.getStartPage())
                            .endPage(dailyMission.getEndPage())
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
     * @param member
     */
    public void completeMission(Long dailyMissionId, BookClubMember member, Board board) {
        // 미션 찾기
        DailyMission dailyMission = dailyMissionRepository.findById(dailyMissionId)
                .orElseThrow(() -> new RuntimeException("Mission not found"));

        // 해당 유저가 이미 이 미션을 완료했는지 확인
        boolean alreadyCompleted = dailyMissionCompletionRepository.existsByDailyMissionAndMember(dailyMission, member);

        if (alreadyCompleted) {
            throw new CustomException(ErrorCode.MISSION_ALREADY_COMPLETED); // 적절한 에러 메시지를 추가
        }

        // DailyMission의 날짜와 게시글의 작성 날짜가 일치하는지 확인
        if (!dailyMission.getMissionDate().equals(board.getCreatedAt().toLocalDate())) {
            throw new CustomException(ErrorCode.MISSION_DATE_MISMATCH);  // 미션 날짜와 게시글 작성 날짜가 다를 경우 예외 처리
        }

        // 미션 완료 처리
        DailyMissionCompletion completion = DailyMissionCompletion.builder()
                .dailyMission(dailyMission)
                .member(member)
                .completionDate(LocalDate.now())  // 현재 날짜로 완료 날짜 설정
                .build();

        // 완료된 미션 저장
        dailyMissionCompletionRepository.save(completion);
    }

    @Transactional
    public void scheduleService(){
        LocalDate today = LocalDate.now();

        List<BookClubChallenge> bookClubChallenges = bookClubChallengeRepository.findAllByDelYn("N");

        for(BookClubChallenge bookClubChallenge : bookClubChallenges){

            DailyMission dailyMission = dailyMissionRepository.findByChallengeAndMissionDate(bookClubChallenge, today);
            Long todayPage = (long) dailyMission.getEndPage();

            Book book = bookRepository.findByIsbn13(bookClubChallenge.getBook().getIsbn13())
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_BOOK));

            Long totalPage = book.getTotalPages();

            int percentage = (int) ((todayPage/totalPage)*100);

            BookClubChallenge updatedChallenge = BookClubChallenge.builder()
                    .challengeId(bookClubChallenge.getChallengeId())
                    .bookClub(bookClubChallenge.getBookClub())
                    .book(bookClubChallenge.getBook())
                    .startDate(bookClubChallenge.getStartDate())
                    .endDate(bookClubChallenge.getEndDate())
                    .progressPercentage(percentage)
                    .build();

            // 업데이트된 객체 저장
            bookClubChallengeRepository.save(updatedChallenge);
        }

    }



}

