    package com.readmate.ReadMate.bookclub.service;


    import com.readmate.ReadMate.book.entity.Book;
    import com.readmate.ReadMate.book.repository.BookRepository;
    import com.readmate.ReadMate.book.service.BookService;
    import com.readmate.ReadMate.bookclub.dto.req.BookClubRequest;
    import com.readmate.ReadMate.bookclub.dto.req.BookInfoRequest;
    import com.readmate.ReadMate.bookclub.dto.res.BookClubChallengeResponse;
    import com.readmate.ReadMate.bookclub.dto.res.BookClubListResponse;
    import com.readmate.ReadMate.bookclub.dto.res.BookClubResponse;
    import com.readmate.ReadMate.bookclub.entity.*;
    import com.readmate.ReadMate.bookclub.repository.BookClubChallengeRepository;
    import com.readmate.ReadMate.bookclub.repository.BookClubMemberRepository;
    import com.readmate.ReadMate.bookclub.repository.BookClubRepository;
    import com.readmate.ReadMate.bookclub.repository.DailyMissionRepository;
    import com.readmate.ReadMate.common.exception.CustomException;
    import com.readmate.ReadMate.common.exception.enums.ErrorCode;
    import com.readmate.ReadMate.login.security.CustomUserDetails;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.data.domain.Sort;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;


    import java.time.LocalDate;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.UUID;
    import java.util.stream.Collectors;

    import static com.readmate.ReadMate.common.exception.enums.ErrorCode.INVALID_CLUB;


    @Service
    @Slf4j
    @RequiredArgsConstructor
    @Transactional
    public class BookClubService {

        private final BookClubRepository bookClubRepository;
        private final BookRepository bookRepository;
        private final BookClubMemberRepository bookClubMemberRepository;
        private final BookClubChallengeService bookClubChallengeService;
        private final BookService bookService;
        private final DailyMissionRepository dailyMissionRepository;
        private final BookClubChallengeRepository bookClubChallengeRepository;

        /**
         * 북클럽 생성 로직
         * @param userDetails
         * @param clubRequest
         * @return
         */
        public Long createClub(CustomUserDetails userDetails, BookClubRequest clubRequest) {
            //북클럽 이름 중복 여부 확인
            if(isBookClubNameTaken(clubRequest.getBookClubName())){
                throw new CustomException(ErrorCode.DUPLICATE_CLUB_NAME);
            }

            // 북클럽의 시작일과 종료일 검증
            if (clubRequest.getStartDate().isAfter(clubRequest.getEndDate())) {
                throw new CustomException(ErrorCode.INVALID_CLUB_DATES);
            }

            //챌린지 날짜 중복되는지 검증
            validateBookDatesOverlap(clubRequest);

            // 챌린지 날짜, 진행 날짜에 대한 검증
            for (BookInfoRequest bookInfoRequest : clubRequest.getBookList()) {
                validateBookDates(bookInfoRequest, clubRequest);
            }

            //북클럽 생성
            BookClub bookClub = new BookClub();
            bookClub.createBookClub(clubRequest);

            // 북클럽 키 생성 후, BookClub 객체에 설정
            String bookClubKey = createBookClubKey();
            bookClub.setBookClubKey(bookClubKey);

            //Leader ID 를 북클럽 생성한 USER ID 로 생성
            Long leaderId = userDetails.getUser().getUserId();
            bookClub.setLeaderId(leaderId);

            //북클럽 생성하고, Leader 가입
            BookClub savedBookClub = bookClubRepository.save(bookClub);
            joinLeader(savedBookClub, leaderId);

            //책 ISBN13 받아서 Book에 저장
            List<Book> bookClubBooks = clubRequest.getBookList().stream()
                    .map(bookInfo -> bookService.saveBookByIsbn(bookInfo.getIsbn())) // ISBN으로 책 저장
                    .collect(Collectors.toList());

            // 책 저장 후, 각각의 책에 대해 챌린지 생성
            for (int i = 0; i < bookClubBooks.size(); i++) {
                Book savedBook = bookClubBooks.get(i);
                BookInfoRequest bookInfoRequest = clubRequest.getBookList().get(i);

                // 각각의 책에 대해 챌린지 생성
                bookClubChallengeService.createClubChallenge(
                        savedBookClub,  // 북클럽
                        savedBook,      // 저장된 책
                        bookInfoRequest.getReadingStartDate(),  // 시작일
                        bookInfoRequest.getReadingEndDate()     // 종료일
                );
            }

            return savedBookClub.getBookClubId();
        }

        private void validateBookDates(BookInfoRequest bookInfoRequest, BookClubRequest clubRequest) {
            // 책 ReadingStartDate가 북클럽 StartDate보다 빠르면 에러 발생
            if (bookInfoRequest.getReadingStartDate().isBefore(clubRequest.getStartDate())) {
                throw new CustomException(ErrorCode.INVALID_BOOK_DATE);
            }

            // 책 ReadingEndDate가 북클럽 EndDate보다 늦으면 에러 발생
            if (bookInfoRequest.getReadingEndDate().isAfter(clubRequest.getEndDate())) {
                throw new CustomException(ErrorCode.INVALID_BOOK_DATE);
            }
        }

        public String createBookClubKey() {
            return UUID.randomUUID().toString().substring(0, 15);
        }


        // 책 날짜 중복 검증
        private void validateBookDatesOverlap(BookClubRequest clubRequest) {
            List<BookInfoRequest> bookList = clubRequest.getBookList();
            List<LocalDate[]> dateRanges = new ArrayList<>();

            for (BookInfoRequest bookInfoRequest : bookList) {
                LocalDate startDate = bookInfoRequest.getReadingStartDate();
                LocalDate endDate = bookInfoRequest.getReadingEndDate();

                // 이미 추가된 책들의 날짜 범위와 겹치는지 확인
                for (LocalDate[] existingRange : dateRanges) {
                    LocalDate existingStart = existingRange[0];
                    LocalDate existingEnd = existingRange[1];

                    if (isOverlapping(existingStart, existingEnd, startDate, endDate)) {
                        throw new CustomException(ErrorCode.OVERLAPPING_BOOK_DATES);
                    }
                }

                // 현재 책의 날짜 범위를 리스트에 추가
                dateRanges.add(new LocalDate[]{startDate, endDate});
            }
        }

        // 날짜 범위가 겹치는지 확인하는 메서드
        private boolean isOverlapping(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
            return !start1.isAfter(end2) && !start2.isAfter(end1);
        }

        /**
         * 북클럽 수정 로직
         * @param userDetails
         * @param clubId
         * @param clubRequest
         * @return
         */
        public Long updateClub(CustomUserDetails userDetails, Long clubId, BookClubRequest clubRequest) {

            // 책 날짜 중복 검증
            validateBookDatesOverlap(clubRequest);

            // 책 날짜 범위 검증
            for (BookInfoRequest bookInfoRequest : clubRequest.getBookList()) {
                validateBookDates(bookInfoRequest, clubRequest);
            }

            // 수정할 북클럽을 조회
            BookClub savedBookClub = bookClubRepository.findById(clubId)
                    .orElseThrow(() -> new CustomException(INVALID_CLUB));

            Long userId = userDetails.getUser().getUserId();

            // 리더 여부 확인
            if (!userId.equals(savedBookClub.getLeaderId())) {
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }

            // 리더 변경 로직
            if (!clubRequest.getLeaderId().equals(savedBookClub.getLeaderId())) {

                // 새로운 리더가 북클럽의 멤버인지 확인
                BookClubMember newLeader = bookClubMemberRepository.findByUserIdAndBookClub(clubRequest.getLeaderId(), savedBookClub);
                if (newLeader == null) {
                    throw new CustomException(ErrorCode.NOT_MEMBER);
                }

                // 기존 리더 조회
                BookClubMember oldLeader = bookClubMemberRepository.findByUserIdAndBookClub(savedBookClub.getLeaderId(), savedBookClub);
                if (oldLeader == null) {
                    throw new CustomException(ErrorCode.INVALID_CLUB_MEMBER);  // 리더가 없는 경우도 예외 처리
                }

                // 새 리더가 승인된 멤버이며 삭제되지 않은 상태인지 확인
                if (!newLeader.getIsApprove() || "Y".equals(newLeader.getDelYn())) {
                    throw new CustomException(ErrorCode.NOT_APPROVED_MEMBER);
                }

                // 리더 변경
                savedBookClub.setLeaderId(clubRequest.getLeaderId());
                newLeader.setClubMemberRole(BookClubMemberRole.LEADER);
                oldLeader.setClubMemberRole(BookClubMemberRole.MEMBER);

                bookClubMemberRepository.save(newLeader);
                bookClubMemberRepository.save(oldLeader);
            }

            // 북클럽 정보 수정
            savedBookClub.updateBookClub(clubRequest);

            // 책 ISBN13로 책 저장
            List<Book> bookClubBooks = clubRequest.getBookList().stream()
                    .map(bookInfo -> bookService.saveBookByIsbn(bookInfo.getIsbn()))
                    .collect(Collectors.toList());

            // 기존 챌린지 삭제 처리
            List<BookClubChallenge> challenges = bookClubChallengeRepository.findAllByBookClub(savedBookClub);
            for (BookClubChallenge challenge : challenges) {
                challenge.delete();

                // 데일리 미션 삭제
                List<DailyMission> dailyMissionList = dailyMissionRepository.findAllByChallenge(challenge);
                dailyMissionRepository.deleteAll(dailyMissionList);
            }

            bookClubChallengeRepository.saveAll(challenges);

            // 새로운 챌린지 생성
            for (int i = 0; i < bookClubBooks.size(); i++) {
                Book savedBook = bookClubBooks.get(i);
                BookInfoRequest bookInfoRequest = clubRequest.getBookList().get(i);

                // 책의 시작 날짜가 북클럽 시작 날짜 이전인 경우 예외 처리
                if (bookInfoRequest.getReadingStartDate().isBefore(savedBookClub.getStartDate())) {
                    throw new CustomException(ErrorCode.INVALID_BOOK_DATE);
                }

                // 책의 종료 날짜가 북클럽 종료 날짜 이후인 경우 예외 처리
                if (bookInfoRequest.getReadingEndDate().isAfter(savedBookClub.getEndDate())) {
                    throw new CustomException(ErrorCode.INVALID_BOOK_DATE);
                }

                // 챌린지 생성
                bookClubChallengeService.createClubChallenge(
                        savedBookClub,
                        savedBook,
                        bookInfoRequest.getReadingStartDate(),
                        bookInfoRequest.getReadingEndDate()
                );
            }

            return savedBookClub.getBookClubId();
        }


        /**
         * 북클럽 삭제 로직
         * @param userDetails
         * @param clubId
         * @return
         */
        public String deleteClub(CustomUserDetails userDetails, Long clubId) {
            BookClub bookClub = bookClubRepository.findById(clubId)
                    .orElseThrow(() -> new CustomException(INVALID_CLUB));

            // UserID 가 리더 아이디와 같은지 확인 - 권한 여부
            if (!userDetails.getUser().getUserId().equals(bookClub.getLeaderId())) {
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }
            //북클럽이 이미 삭제 되었으면
            if (bookClub.getDelYn().equals("Y")) {
                throw new CustomException(ErrorCode.ALREADY_DELETED);
            }
            //북클럽  챌린지, 데일리 미션 제러
            List<BookClubChallenge> challenges = bookClubChallengeRepository.findAllByBookClub(bookClub);
            for (BookClubChallenge challenge : challenges) {
                challenge.delete();

                List<DailyMission> dailyMissionList = dailyMissionRepository.findAllByChallenge(challenge);
                dailyMissionRepository.deleteAll(dailyMissionList);
            }

            bookClub.delete();
            bookClubRepository.save(bookClub);

            return "북클럽 삭제 완료되었습니다";
        }

        /**
         * 북클럽 리스트 조회
         * @return
         */
        public List<BookClubListResponse> getClubList() {
            List<BookClub> bookClubs = bookClubRepository.findAllByDelYn("N");


            return bookClubs.stream()
                    .map(bookClub -> BookClubListResponse.builder()
                            .bookClubID(bookClub.getBookClubId())
                            .bookClubName(bookClub.getBookClubName())
                            .description(bookClub.getDescription())
                            .startDate(bookClub.getStartDate())
                            .endDate(bookClub.getEndDate())
                            .recruitmentStartDate(bookClub.getRecruitmentStartDate())
                            .recruitmentEndDate(bookClub.getRecruitmentEndDate())
                            .bookCover(getCurrentChallenge(LocalDate.now(), bookClub.getBookClubId()).getBook().getBookCover())
                            .build())
                    .toList();

        }



        /**
         * 북클럽 개별 조회
         * @param clubId
         * @return
         */
        public BookClubResponse getBookClub(Long clubId) {
            BookClub bookClub = bookClubRepository.findById(clubId)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CLUB));

            List<BookClubChallenge> challengeList = bookClubChallengeRepository.findAllByDelYnAndBookClub("N",bookClub);
            List<BookClubChallengeResponse> challengeResponses = getChallengeResponses(challengeList);

            BookClubResponse bookClubResponse = new BookClubResponse();
            bookClubResponse.createBookClubResponse(bookClub, challengeResponses);
            return bookClubResponse;
        }

        public List<BookClubChallengeResponse> getChallengeResponses(List<BookClubChallenge> challenges) {
            return challenges.stream()
                    .map(challenge -> {
                        Book book = challenge.getBook();
                        return BookClubChallengeResponse.builder()
                                .readingStartDate(challenge.getStartDate())
                                .readingEndDate(challenge.getEndDate())
                                .bookCover(book.getBookCover())
                                .bookTitle(book.getTitle())
                                .build();
                    })
                    .collect(Collectors.toList());
        }

        // 활성 챌린지 가져오기
        public BookClubChallenge getCurrentChallenge(LocalDate currentDate, Long bookClubId) {
            BookClub bookClub = bookClubRepository.findById(bookClubId)
                    .orElseThrow(() -> new CustomException(INVALID_CLUB));
            List<BookClubChallenge> challenges = bookClubChallengeRepository.findAllByBookClub(bookClub);
            return challenges.stream()
                    .filter(challenge -> !challenge.getStartDate().isAfter(currentDate) &&
                            !challenge.getEndDate().isBefore(currentDate))
                    .findFirst()
                    .orElse(null); // 현재 날짜에 맞는 챌린지가 없으면 null 반환
        }

        /**
         * 북클럼 이름 중복 여부
         * @param clubName
         * @return
         */
        public boolean isBookClubNameTaken(String clubName) {
            return bookClubRepository.existsByBookClubName(clubName);
        }


        /**
         * 리더 북클럽 멤버 Repo 추가하는 로직
         * @param bookClub
         * @return String
         */
        public void joinLeader(BookClub bookClub,  Long leaderId) {

                    BookClubMember bookClubMember = BookClubMember.builder()
                            .userId(leaderId)
                            .clubMemberRole(BookClubMemberRole.LEADER)
                            .bookClub(bookClub)
                            .joinMessage("북클럽 리더 입니다")
                            .isApprove(true)
                            .build();
                    bookClubMemberRepository.save(bookClubMember);
            }

        /**
         * 북클럽 증가 메소드
         */
        public void incrementViewCount(Long bookClubId) {
            BookClub bookClub = bookClubRepository.findById(bookClubId)
                    .orElseThrow(() -> new CustomException(INVALID_CLUB));
            bookClub.setViewCount(bookClub.getViewCount() + 1);

            bookClubRepository.save(bookClub);
        }

        public List<BookClub> getBookClubsOrderedByViewCount() {
            return bookClubRepository.findAll(Sort.by(Sort.Direction.DESC, "viewCount"));
        }

            



    }




