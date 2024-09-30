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
    import com.readmate.ReadMate.common.exception.CustomException;
    import com.readmate.ReadMate.common.exception.enums.ErrorCode;
    import com.readmate.ReadMate.login.security.CustomUserDetails;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;


    import java.time.LocalDate;
    import java.util.List;
    import java.util.UUID;
    import java.util.stream.Collectors;


    @Service
    @Slf4j
    @RequiredArgsConstructor
    public class BookClubService {

        private final BookClubRepository bookClubRepository;
        private final BookRepository bookRepository;
        private final BookClubMemberRepository bookClubMemberRepository;
        private final BookClubChallengeService bookClubChallengeService;
        private final BookService bookService;

        private final BookClubChallengeRepository bookClubChallengeRepository;


        public Long createClub(CustomUserDetails userDetails, BookClubRequest clubRequest) {

            BookClub bookClub = new BookClub();
            bookClub.createBookClub(clubRequest);

            // 책 리스트에 대한 날짜 검증
            for (BookInfoRequest bookInfoRequest : clubRequest.getBookList()) {
                validateBookDates(bookInfoRequest, clubRequest);
            }



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

        @Transactional
        public Long updateClub(CustomUserDetails userDetails, Long clubId, BookClubRequest clubRequest) {

            // BookClub 수정
            BookClub savedBookClub = bookClubRepository.findById(clubId)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CLUB));

            Long userId = userDetails.getUser().getUserId();

            //사용자가 리더인지 확인
            if (userId.equals(savedBookClub.getLeaderId())) {
                if(!clubRequest.getLeaderId().equals(savedBookClub.getLeaderId())) {
                    // 기존 리더와 새 리더 조회
                    BookClubMember newLeader = bookClubMemberRepository.findByUserIdAndBookClub(clubRequest.getLeaderId(), savedBookClub);
                    BookClubMember oldLeader = bookClubMemberRepository.findByUserIdAndBookClub(clubRequest.getLeaderId(), savedBookClub);

                    // 선택된 리더가 북클럽의 멤버이며 삭제되지 않은 상태일 경우
                    if (newLeader.getIsApprove() && newLeader.getDelYn().equals("N")) {

                        savedBookClub.setLeaderId(clubRequest.getLeaderId());

                        newLeader.setClubMemberRole(BookClubMemberRole.LEADER);
                        oldLeader.setClubMemberRole(BookClubMemberRole.MEMBER);

                        bookClubMemberRepository.save(newLeader);
                        bookClubMemberRepository.save(oldLeader);

                    } else {
                        throw new CustomException(ErrorCode.NOT_MEMBER);
                    }
                }
                savedBookClub.updateBookClub(clubRequest);

                //책 ISBN13 받아서 Book에 저장
                List<Book> bookClubBooks = clubRequest.getBookList().stream()
                        .map(bookInfo -> bookService.saveBookByIsbn(bookInfo.getIsbn()))
                        .collect(Collectors.toList());


                // 이미 존재하던 Challenge delYn Y로 처리
                List<BookClubChallenge> challenges = bookClubChallengeRepository.findAllByBookClub(savedBookClub);

                for (BookClubChallenge challenge : challenges) {
                    challenge.delete();
                }
                bookClubChallengeRepository.saveAll(challenges);


                // 각각의 책에 대해 챌린지 새로 생성
                for (int i = 0; i < bookClubBooks.size(); i++) {
                    Book savedBook = bookClubBooks.get(i);
                    BookInfoRequest bookInfoRequest = clubRequest.getBookList().get(i);

                    // 책 ReadingStartDate가 북클럽 StartDate보다 빠르면 에러 발생
                    if (bookInfoRequest.getReadingStartDate().isBefore(savedBookClub.getStartDate())) {
                        throw new CustomException(ErrorCode.INVALID_BOOK_DATE);
                    }

                    // 책 ReadingEndDate가 북클럽 EndDate보다 늦으면 에러 발생
                    if (bookInfoRequest.getReadingEndDate().isAfter(savedBookClub.getEndDate())) {
                        throw new CustomException(ErrorCode.INVALID_BOOK_DATE);
                    }


                    // 각각의 책에 대해 챌린지 생성
                    bookClubChallengeService.createClubChallenge(
                            savedBookClub,  // 북클럽
                            savedBook,      // 저장된 책
                            bookInfoRequest.getReadingStartDate(),  // 시작일
                            bookInfoRequest.getReadingEndDate()     // 종료일
                    );
                }

                return savedBookClub.getBookClubId();
            } else {
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }

        }

        public String deleteClub(CustomUserDetails userDetails, Long clubId) {
            BookClub bookClub = bookClubRepository.findById(clubId)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CLUB));

            // 리더인지 확인
            if (userDetails.getUser().getUserId().equals(bookClub.getLeaderId())) {
                // 이미 삭제된 경우 예외 처리
                if (bookClub.getDelYn().equals("Y")) {
                    throw new CustomException(ErrorCode.ALREADY_DELETED);
                }

                // 북클럽의 챌린지에서 책 정보를 가져와서 비활성화
                List<BookClubChallenge> challenges = bookClubChallengeRepository.findAllByBookClub(bookClub);
                for (BookClubChallenge challenge : challenges) {
                    challenge.delete();
                }

                // 북클럽 삭제
                bookClub.delete();
                bookClubRepository.save(bookClub); // 북클럽 저장

                return "북클럽 삭제 완료되었습니다";
            } else {
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }
        }

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

        // 활성 챌린지 가져오기
        public BookClubChallenge getCurrentChallenge(LocalDate currentDate, Long bookClubId) {
            BookClub bookClub = bookClubRepository.findById(bookClubId)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CLUB));
            List<BookClubChallenge> challenges = bookClubChallengeRepository.findAllByBookClub(bookClub);
            return challenges.stream()
                    .filter(challenge -> !challenge.getStartDate().isAfter(currentDate) &&
                            !challenge.getEndDate().isBefore(currentDate))
                    .findFirst()
                    .orElse(null); // 현재 날짜에 맞는 챌린지가 없으면 null 반환
        }


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

        public boolean isBookClubNameTaken(String clubName) {
            return bookClubRepository.existsByBookClubName(clubName);
        }

//        public Long calculateTotalPages( Long bookClubId,List<BookClubBook> bookClubBooks){
//            List<Book> books = bookRepository.findAllByBookClubIdAndIsActive(bookClubId, true);
//
//            Long totalPages = books.stream()
//                    .mapToLong(BookClubBook::getPages) // Get the total pages for each book
//                    .sum();
//            return totalPages;
//        }


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



    }




