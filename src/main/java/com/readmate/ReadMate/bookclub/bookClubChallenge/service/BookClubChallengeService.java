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


    public void createClubChallenge(final long bookClubId, final String  isbn13, LocalDate startDate, LocalDate endDate) {
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
        DailyMission dailyMission = bookClubMissionService
                .getByChallengeAndDate(bookClubChallenge.getChallengeId(), today);

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
    public List<UserMissionResponse> getUserChallenge(final long userId) {

        List<Long> bookClubIds = bookClubMemberService.findBookClubIdsByUserId(userId);
        LocalDate today = LocalDate.now();

    // 2. 북클럽 내 오늘의 Mission 조회 및 UserMissionResponse 생성
        return bookClubIds.stream()
                .map(bookClubId -> {
                    // bookClubId로 BookClub 정보를 가져옴
                    BookClub bookClub = bookClubRepository.findById(bookClubId)
                            .orElseThrow( () ->  new CustomException(ErrorCode.INVALID_CLUB));

                    System.out.println("bookClub.getBookClubId()+today = " + bookClub.getBookClubId()+today);

                    // 해당 bookClubId로 오늘의 Challenge 조회
                    Optional<BookClubChallenge> optionalBookClubChallenge = bookClubChallengeRepository
                            .findCurrentChallengeByBookClubIdAndDate(bookClub.getBookClubId(), today);

                   if(optionalBookClubChallenge.isEmpty()){
                       return null;
                   }

                    BookClubChallenge bookClubChallenge = optionalBookClubChallenge.get();

                    // Challenge에 대한 도서 정보 조회
                    BookResponse bookResponse = bookService.getBookByIsbn(bookClubChallenge.getIsbn13());

                    // 오늘 날짜의 DailyMission 조회
                    DailyMission dailyMission = bookClubMissionService
                            .getByChallengeAndDate(bookClubChallenge.getChallengeId(), today);

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

        List<BookClubChallenge> bookClubChallenges = bookClubChallengeRepository.findAllByDelYnFalse();

        if(bookClubChallenges.isEmpty()){
            return;
        }

        for(BookClubChallenge bookClubChallenge : bookClubChallenges){

            DailyMission dailyMission = bookClubMissionService.getByChallengeAndDate(bookClubChallenge.getChallengeId(), today);
            Long todayPage = (long) dailyMission.getEndPage();

            BookResponse bookResponse = bookService.getBookByIsbn(bookClubChallenge.getIsbn13());

            Long totalPage = bookResponse.getTotalPages();

            int percentage = (int) ((todayPage/totalPage)*100);

            BookClubChallenge updatedChallenge = BookClubChallenge.builder()
                    .challengeId(bookClubChallenge.getChallengeId())
                    .bookClubId(bookClubChallenge.getBookClubId())
                    .isbn13(bookClubChallenge.getIsbn13())
                    .startDate(bookClubChallenge.getStartDate())
                    .endDate(bookClubChallenge.getEndDate())
                    .progressPercentage(percentage)
                    .build();

            // 업데이트된 객체 저장
            bookClubChallengeRepository.save(updatedChallenge);
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

