    package com.readmate.ReadMate.bookclub.service;


    import com.readmate.ReadMate.bookclub.dto.req.BookClubRequest;
    import com.readmate.ReadMate.bookclub.dto.res.BookClubBookResponse;
    import com.readmate.ReadMate.bookclub.dto.res.BookClubListResponse;
    import com.readmate.ReadMate.bookclub.dto.res.BookClubResponse;
    import com.readmate.ReadMate.bookclub.entity.BookClub;
    import com.readmate.ReadMate.bookclub.entity.BookClubBook;
    import com.readmate.ReadMate.bookclub.entity.BookClubMember;
    import com.readmate.ReadMate.bookclub.entity.BookClubMemberRole;
    import com.readmate.ReadMate.bookclub.repository.BookClubBookRepository;
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
    import java.util.UUID;
    import java.util.stream.Collectors;


    @Service
    @Slf4j
    @RequiredArgsConstructor
    public class BookClubService {

        private final BookClubRepository bookClubRepository;
        private final BookClubBookRepository bookClubBookRepository;
        private final BookClubMemberRepository bookClubMemberRepository;

        public Long createClub(CustomUserDetails userDetails, BookClubRequest clubRequest) {

            BookClub bookClub = new BookClub();
            bookClub.createBookClub(clubRequest);

            // 북클럽 키 생성 후, BookClub 객체에 설정
            String bookClubKey = createBookClubKey();
            bookClub.setBookClubKey(bookClubKey);

            Long leaderId = userDetails.getUser().getUserId();
            bookClub.setLeaderId(leaderId);
            bookClub.setCurrentBookId(clubRequest.getCurrentBookId());

            BookClub savedBookClub = bookClubRepository.save(bookClub);
            joinLeader(savedBookClub.getBookClubId(),leaderId);
            List<BookClubBook> bookClubBooks = clubRequest.getBookList().stream()
                    .map(isbn -> BookClubBook.builder()
                            .isbn(isbn)
                            .bookClub(savedBookClub)
                            .build())
                    .collect(Collectors.toList());

            bookClubBookRepository.saveAll(bookClubBooks);

            Long totalPages = calculateTotalPages(savedBookClub.getBookClubId(), bookClubBooks);
            bookClub.setTotalPages(totalPages);

            return savedBookClub.getBookClubId();
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
                if(userId.equals(savedBookClub.getLeaderId())) {

                    BookClubMember newLeader =  bookClubMemberRepository.findByUserIdAndBookClubId(clubRequest.getLeaderId(), savedBookClub.getBookClubId());
                    BookClubMember oldLeader =  bookClubMemberRepository.findByUserIdAndBookClubId(clubRequest.getLeaderId(), savedBookClub.getBookClubId());

                    // 선택된 리더가 북클럽의 멤버이며 삭제되지 않은 상태일 경우
                    if (newLeader != null && newLeader.getIsApprove() && newLeader.getDelYn().equals("N")) {

                        savedBookClub.setLeaderId(clubRequest.getLeaderId());

                        newLeader.setClubMemberRole(BookClubMemberRole.LEADER);
                        oldLeader.setClubMemberRole(BookClubMemberRole.MEMBER);

                        bookClubMemberRepository.save(newLeader);
                        bookClubMemberRepository.save(oldLeader);

                    } else {
                        throw new CustomException(ErrorCode.NOT_MEMBER);
                    }
                    savedBookClub.updateBookClub(clubRequest);

                    List<BookClubBook> savedBooks = bookClubBookRepository.findAllByBookClubId(clubId);
                    savedBooks.forEach(BookClubBook::update);

                    // 새로운 ISBN 리스트로 책을 업데이트
                    List<Long> newIsbns = clubRequest.getBookList();

                    // 기존 책 목록에서 새로운 ISBN과 일치하는 책은 다시 활성화
                    for (BookClubBook book : savedBooks) {
                        if (newIsbns.contains(book.getIsbn())) {
                            book.setActive(true);
                            newIsbns.remove(book.getIsbn());
                        }
                    }

                    List<BookClubBook> newBooks = newIsbns.stream()
                            .map(isbn -> BookClubBook.builder()
                                    .isbn(isbn)
                                    .bookClub(savedBookClub)
                                    .isActive(true)
                                    .build())
                            .collect(Collectors.toList());

                    bookClubBookRepository.saveAll(newBooks); // 비활성화 된 책 업데이트 및 새로운 책 저장
                    return savedBookClub.getBookClubId();
                }else{
                    throw new CustomException(ErrorCode.ACCESS_DENIED);
            }

            }

        public String deleteClub(CustomUserDetails userDetails, Long clubId) {
            BookClub bookClub = bookClubRepository.findById(clubId)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CLUB));

            if(userDetails.getUser().getUserId().equals(bookClub.getLeaderId())){
            if (bookClub.getDelYn().equals("N")) {
                //User가 리더가 아닐 경우 처리 필요 -> UserDetail 구현 되면 추가할 예정

                List<BookClubBook> savedBooks = bookClubBookRepository.findAllByBookClubId(clubId);
                savedBooks.forEach(BookClubBook::update); // 모든 책 비활성화
                bookClub.delete();

                bookClubBookRepository.saveAll(savedBooks);
                bookClubRepository.save(bookClub);
                return "북클럽 삭제 완료되었습니다";
            } else {
                throw new CustomException(ErrorCode.ALREADY_DELETED);
            }

            }else{
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }
        }

        public List<BookClubListResponse> getClubList() {
            List<BookClub> bookClubs = bookClubRepository.findAll();

            return  bookClubs.stream()
                    .map(bookClub -> BookClubListResponse.builder()
                            .bookClubID(bookClub.getBookClubId())
                            .bookClubName(bookClub.getBookClubName())
                            .description(bookClub.getDescription())
                            .bookClubImageID(bookClub.getBookClubImageID())
                            .startDate(bookClub.getStartDate())
                            .endDate(bookClub.getEndDate())
                            .recruitmentStartDate(bookClub.getRecruitmentStartDate())
                            .recruitmentEndDate(bookClub.getRecruitmentEndDate())
                            .bookClubGenres(bookClub.getBookClubGenre())
                            .currentBookId(bookClub.getCurrentBookId())
                            .build())
                    .toList();

        }


        public BookClubResponse getBookClub(Long clubId) {
            BookClub bookClub = bookClubRepository.findById(clubId)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CLUB));


            List<BookClubBookResponse> bookClubBooks = bookClubBookRepository.findAllByBookClubIdAndIsActive(clubId, true).stream()
                    .map(book -> BookClubBookResponse.builder()
                            .bookClubBookId(book.getId())
                            .isbn(book.getIsbn())
                            .isActive(book.isActive())
                            .totalPage(book.getPages())
                            .build())
                    .collect(Collectors.toList());

            BookClubResponse bookClubResponse = new BookClubResponse();
            bookClubResponse.createBookClubResponse(bookClub,bookClubBooks);

            return bookClubResponse;

        }

        public boolean isBookClubNameTaken(String clubName) {
            return bookClubRepository.existsByBookClubName(clubName);
        }

        public Long calculateTotalPages( Long bookClubId,List<BookClubBook> bookClubBooks){
            List<BookClubBook> books = bookClubBookRepository.findAllByBookClubIdAndIsActive(bookClubId, true);

            Long totalPages = books.stream()
                    .mapToLong(BookClubBook::getPages) // Get the total pages for each book
                    .sum();
            return totalPages;
        }



        /**
         * 리더 북클럽 멤버 Repo 추가하는 로직
         * @param bookClubId
         * @return String
         */
        public void joinLeader(Long bookClubId,  Long leaderId) {

                    BookClubMember bookClubMember = BookClubMember.builder()
                            .userId(leaderId)
                            .clubMemberRole(BookClubMemberRole.LEADER)
                            .bookClubId(bookClubId)
                            .joinMessage("북클럽 리더 입니다")
                            .isApprove(true)
                            .build();
                    bookClubMemberRepository.save(bookClubMember);
            }

    }


