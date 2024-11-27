package com.readmate.ReadMate.bookclub.bookClubChallenge.service;

import com.readmate.ReadMate.book.dto.res.BookResponse;
import com.readmate.ReadMate.book.entity.Book;
import com.readmate.ReadMate.book.service.BookService;
import com.readmate.ReadMate.bookclub.bookClubChallenge.entity.BookClubChallenge;
import com.readmate.ReadMate.bookclub.bookClubChallenge.repository.BookClubChallengeRepository;
import com.readmate.ReadMate.bookclub.bookClubMember.service.BookClubMemberService;
import com.readmate.ReadMate.bookclub.club.dto.req.BookInfoRequest;
import com.readmate.ReadMate.bookclub.club.entity.BookClub;
import com.readmate.ReadMate.bookclub.club.repository.BookClubRepository;
import com.readmate.ReadMate.bookclub.dailyMission.dto.ChallengeResponse;
import com.readmate.ReadMate.bookclub.dailyMission.entity.DailyMission;
import com.readmate.ReadMate.bookclub.dailyMission.dto.UserMissionResponse;
import com.readmate.ReadMate.bookclub.dailyMission.service.BookClubMissionService;
import com.readmate.ReadMate.common.exception.CustomException;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;

import lombok.AllArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
@Transactional
public class BookClubChallengeService {

    private  final BookClubChallengeRepository bookClubChallengeRepository;
    private  final BookService bookService;
    private final BookClubMissionService bookClubMissionService;
    private final BookClubMemberService bookClubMemberService;
    private final BookClubRepository bookClubRepository;


    public void createClubChallenge(final long bookClubId, long isbn13, LocalDate startDate, LocalDate endDate) {
        BookClubChallenge bookClubChallenge =  BookClubChallenge
                        .builder()
                        .bookClubId(bookClubId)
                        .startDate(startDate)
                        .endDate(endDate)
                        .isbn13(isbn13)
                        .build();

        BookClubChallenge savedBookClubChallenge =bookClubChallengeRepository.save(bookClubChallenge);
        bookClubMissionService.createMissionsForChallenge(savedBookClubChallenge);
    }

    // 오늘의 진행 상황 - 북클럽 Detail 페이지에 띄울
    @Transactional
    public ChallengeResponse getClubChallenge(final long userId, Long bookClubId) {

        //1. 유저가 해당 북클럽의 승인된 멤버인지 확인
        bookClubMemberService.validateApprovedMember(bookClubId,userId);

        //2. 북클럽 챌린지에서 챌린지 정보 가져옴 -> 없을 경우 빈 객체 반환
        LocalDate today = LocalDate.now();
        Optional<BookClubChallenge> optionalChallenge = bookClubChallengeRepository.findCurrentChallengeByBookClubIdAndDate(bookClubId, today);

        // 만약에 없을 경우 뒤의 로직을 처리하지 않고 빈 객체를 return 하거나 없다고 떠야함.
        // bookClubChallenge.getBookClubId() = null 인데 여기서 getIsbn13 을 하려고하니 NPE 가 나는 것

        if(optionalChallenge.isEmpty()){
            return ChallengeResponse.builder().build();
        }

        BookClubChallenge bookClubChallenge = optionalChallenge.get();

        System.out.println("bookClubChallenge.getBookClubId() = " + bookClubChallenge.getBookClubId());

        //3. 가져온 챌린지 정보로 책정보 조회
        BookResponse bookResponse = bookService.getBookByIsbn(bookClubChallenge.getIsbn13());

        //4. DailyMission 찾기
        Optional<DailyMission> dailyMissionOptionalMission = bookClubMissionService
                .getByChallengeAndDate(bookClubChallenge.getChallengeId(), today);

        if(dailyMissionOptionalMission.isEmpty()){
            return ChallengeResponse.builder().build();
        }

        DailyMission dailyMission = dailyMissionOptionalMission.get();


        return ChallengeResponse.builder()
                .missionId(dailyMission.getMissionId())
                .date(dailyMission.getMissionDate())
                .startPage(dailyMission.getStartPage())
                .endPage(dailyMission.getEndPage())
                .title(bookResponse.getTitle())
                .bookCover(bookResponse.getBookCover())
                .progressPercentage(bookClubChallenge.getProgressPercentage())
                .build();
    }

    // 유저가 참여중인 미션 조회
    @Transactional(readOnly = true)
    public List<UserMissionResponse> getUserChallenge(final long userId) {

        List<Long> bookClubIds = bookClubMemberService.findBookClubIdsByUserId(userId);
        LocalDate today = LocalDate.now();

        // 북클럽 내 오늘의 Mission 조회 및 UserMissionResponse 생성
        return bookClubIds.stream()
                .map(bookClubId -> {
                    // bookClubId로 BookClub 정보를 가져옴
                    BookClub bookClub = bookClubRepository.findById(bookClubId)
                            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CLUB));

                    // 해당 bookClubId로 오늘의 Challenge 조회
                    BookClubChallenge bookClubChallenge = bookClubChallengeRepository
                            .findCurrentChallengeByBookClubIdAndDate(bookClub.getBookClubId(), today)
                            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));


                    // Challenge에 대한 도서 정보 조회
                    BookResponse bookResponse = bookService.getBookByIsbn(bookClubChallenge.getIsbn13());

                    // 오늘 날짜의 DailyMission 조회
                    DailyMission dailyMission = bookClubMissionService
                            .getByChallengeAndDate(bookClubChallenge.getChallengeId(), today)
                            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_MISSION));

                    // UserMissionResponse 생성
                    return UserMissionResponse.builder()
                            .missionId(dailyMission.getMissionId())
                            .title(bookClub.getBookClubName()) // 북클럽 이름
                            .date(dailyMission.getMissionDate())
                            .startPage(dailyMission.getStartPage())
                            .endPage(dailyMission.getEndPage())
                            .title(bookResponse.getTitle()) // 도서 제목
                            .bookCover(bookResponse.getBookCover()) // 도서 표지
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }



    @Transactional
    public void scheduleService(){
        LocalDate today = LocalDate.now();

        // 삭제되지 않은 모든 BookClubChallenge를 가져옵니다.
        List<BookClubChallenge> bookClubChallenges = bookClubChallengeRepository.findAllByDelYnFalse();

        if (bookClubChallenges.isEmpty()) {
            return;
        }

        for (BookClubChallenge bookClubChallenge : bookClubChallenges) {

            // 오늘의 DailyMission을 Optional로 가져옵니다.
            Optional <DailyMission> optionalDailyMission = bookClubMissionService.getByChallengeAndDate(bookClubChallenge.getChallengeId(), today);

            // 오늘의 미션이 없으면 건너뜁니다.
            if (optionalDailyMission.isEmpty()) {
                continue;
            }
            DailyMission dailyMission = optionalDailyMission.get();
            // 오늘까지 읽어야 할 페이지 수 (endPage 기준)
            Long todayPage = (long) dailyMission.getEndPage();

            // 도서의 전체 페이지 수
            BookResponse bookResponse = bookService.getBookByIsbn(bookClubChallenge.getIsbn13());
            Long totalPage = bookResponse.getTotalPages();

            // 오늘까지의 진행률 계산
            int percentage = (int) ((double) todayPage / totalPage * 100);

            // 진행률을 업데이트하여 BookClubChallenge 객체를 새로 저장합니다.
            bookClubChallenge.setProgressPercentage(percentage);
            bookClubChallengeRepository.save(bookClubChallenge);  // 수정된 객체 저장
        }
    }


    // BookClubService- 기존 챌린지 삭제 메서드
    public void deleteChallengesAndMissions(Long bookClubId) {
        List<BookClubChallenge> challenges = bookClubChallengeRepository.findAllByBookClubId(bookClubId);
        for (BookClubChallenge challenge : challenges) {
            challenge.delete();
            bookClubMissionService.deleteByChallengeId(challenge.getBookClubId());

        }
        bookClubChallengeRepository.saveAll(challenges);
    }

    //BookClubService-챌린지 생성 메서드
    public void createChallengesForBooks(BookClub savedBookClub, List<Book> bookClubBooks, List<BookInfoRequest> bookInfoRequests) {
        for (int i = 0; i < bookClubBooks.size(); i++) {
            Book savedBook = bookClubBooks.get(i);
            BookInfoRequest bookInfoRequest = bookInfoRequests.get(i);

            if (bookInfoRequest.getReadingStartDate().isBefore(savedBookClub.getStartDate()) ||
                    bookInfoRequest.getReadingEndDate().isAfter(savedBookClub.getEndDate())) {
                throw new CustomException(ErrorCode.INVALID_BOOK_DATE);
            }

            createClubChallenge(
                    savedBookClub.getBookClubId(),
                    savedBook.getIsbn13(),
                    bookInfoRequest.getReadingStartDate(),
                    bookInfoRequest.getReadingEndDate()
            );
        }
    }

}

