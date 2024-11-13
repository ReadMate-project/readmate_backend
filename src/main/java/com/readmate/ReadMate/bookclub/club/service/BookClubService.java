    package com.readmate.ReadMate.bookclub.club.service;

    import com.readmate.ReadMate.book.dto.res.BookResponse;
    import com.readmate.ReadMate.book.entity.Book;

    import com.readmate.ReadMate.book.service.BookService;
    import com.readmate.ReadMate.bookclub.bookClubChallenge.entity.BookClubChallenge;
    import com.readmate.ReadMate.bookclub.bookClubChallenge.service.BookClubChallengeService;

    import com.readmate.ReadMate.bookclub.bookClubMember.service.BookClubMemberService;
    import com.readmate.ReadMate.bookclub.club.dto.req.BookClubRequest;
    import com.readmate.ReadMate.bookclub.club.dto.req.BookInfoRequest;
    import com.readmate.ReadMate.bookclub.bookClubChallenge.dto.BookClubChallengeResponse;
    import com.readmate.ReadMate.bookclub.club.dto.res.BookClubListResponse;
    import com.readmate.ReadMate.bookclub.club.dto.res.BookClubResponse;
    import com.readmate.ReadMate.bookclub.bookClubChallenge.repository.BookClubChallengeRepository;
    import com.readmate.ReadMate.bookclub.club.entity.BookClub;
    import com.readmate.ReadMate.bookclub.club.repository.BookClubRepository;
    import com.readmate.ReadMate.bookclub.club.specification.BookClubSpecification;
    import com.readmate.ReadMate.bookclub.dailyMission.entity.DailyMission;
    import com.readmate.ReadMate.bookclub.dailyMission.repository.DailyMissionRepository;
    import com.readmate.ReadMate.common.exception.CustomException;
    import com.readmate.ReadMate.common.exception.enums.ErrorCode;
    import com.readmate.ReadMate.common.page.PageUtil;
    import com.readmate.ReadMate.login.security.CustomUserDetails;
    import com.readmate.ReadMate.login.service.UserService;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.data.domain.*;
    import org.springframework.data.jpa.domain.Specification;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;


    import java.time.LocalDate;

    import java.util.List;
    import java.util.stream.Collectors;

    import static com.readmate.ReadMate.bookclub.club.validator.BookClubValidator.validate;

    import static com.readmate.ReadMate.bookclub.club.validator.BookClubValidator.validateLeaderPermission;
    import static com.readmate.ReadMate.common.exception.enums.ErrorCode.INVALID_CLUB;


    @Service
    @Slf4j
    @RequiredArgsConstructor
    @Transactional
    public class BookClubService {

        private final BookClubRepository bookClubRepository;
        private final BookClubMemberService bookClubMemberService;
        private final BookClubChallengeService bookClubChallengeService;
        private final BookService bookService;
        private final UserService userService;
        private final DailyMissionRepository dailyMissionRepository;
        private final BookClubChallengeRepository bookClubChallengeRepository;

        public Long createClub(CustomUserDetails userDetails, BookClubRequest clubRequest) {

            //북클럽 이름 중복 여부 확인
            isBookClubNameTaken(clubRequest.getBookClubName());
            //진행 날짜, 모집 날짜에 대한 검증
            validate(clubRequest);

            //북클럽 생성
            BookClub bookClub = new BookClub();
            bookClub.createBookClub(clubRequest);

            //Leader ID 를 북클럽 생성한 USER ID 로 생성
            Long leaderId = userDetails.getUser().getUserId();
            //Leader ID 검증 - userRepo 에 있는지
            userService.getById(leaderId);

            bookClub.setLeaderId(leaderId);

            //북클럽 생성하고, Leader 가입
            BookClub savedBookClub = bookClubRepository.save(bookClub);
            bookClubMemberService.joinLeader(savedBookClub.getBookClubId(), leaderId);

            List<Book> bookClubBooks = saveBooksByIsbn(clubRequest.getBookList());
            bookClubChallengeService.createChallengesForBooks(savedBookClub, bookClubBooks, clubRequest.getBookList());

            return savedBookClub.getBookClubId();
        }

        public Long updateClub(CustomUserDetails userDetails, Long clubId, BookClubRequest clubRequest) {

            // 책 날짜 중복 검증
            validate(clubRequest);

            // 수정할 북클럽을 조회
            BookClub savedBookClub = bookClubRepository.findById(clubId)
                    .orElseThrow(() -> new CustomException(INVALID_CLUB));

            Long userId = userDetails.getUser().getUserId();

            // 리더 여부 확인
            validateLeaderPermission(savedBookClub.getLeaderId(), clubId);
            //리더 변경 메소드 호출
            bookClubMemberService.changeLeader(savedBookClub, userId);
            // 북클럽 정보 수정
            savedBookClub.updateBookClub(clubRequest);

            // 책 ISBN13로 책 저장
            List<Book> bookClubBooks = saveBooksByIsbn(clubRequest.getBookList());
            //기존 챌린지 삭제 메서드 호출
            bookClubChallengeService.deleteChallengesAndMissions(savedBookClub.getBookClubId());
            //새로운 챌린지 생성
            bookClubChallengeService.createChallengesForBooks(savedBookClub, bookClubBooks, clubRequest.getBookList());

            return savedBookClub.getBookClubId();
        }


        public String deleteClub(final CustomUserDetails userDetails, final long bookClubId) {

            BookClub bookClub = bookClubRepository.findById(bookClubId)
                    .orElseThrow(() -> new CustomException(INVALID_CLUB));

            validateLeaderPermission(bookClub.getLeaderId(), userDetails.getUser().getUserId());

            //북클럽이 이미 삭제 되었으면
            if (bookClub.isDelYn()) {
                throw new CustomException(ErrorCode.ALREADY_DELETED);
            }

            //북클럽  챌린지, 데일리 미션
            List<BookClubChallenge> challenges = bookClubChallengeRepository.findAllByBookClubId(bookClubId);
            for (BookClubChallenge challenge : challenges) {
                challenge.delete();

                List<DailyMission> dailyMissionList = dailyMissionRepository.findAllByChallengeId(bookClubId);
                dailyMissionRepository.deleteAll(dailyMissionList);
            }

            bookClub.delete();
            bookClubRepository.save(bookClub);

            return "북클럽 삭제 완료되었습니다";
        }

        // 북클럽 리스트 조회
        public Page<BookClubListResponse> getClubList(final String status, Pageable pageable) {
            LocalDate today = LocalDate.now();

            Specification<BookClub> spec = Specification.where(BookClubSpecification.withDelYnFalse());

            if ("모집중".equals(status)) {
                spec = spec.and(BookClubSpecification.isRecruiting(today));
            } else if ("진행중".equals(status)) {
                spec = spec.and(BookClubSpecification.isInProgress(today));
            }

            // 최신순 조회를 위한 Pageable 추가
            // 따로 Service 단에서 처리한 이유 컨트롤러에서 전달받은 기본 Pageable 객체에 정렬 추가하는 건 Service 가 맞다고 생각
            // 추후에 정렬기준이 주가 될 수 있으니!
            Pageable sortedPageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );

            Page<BookClub> bookClubs = bookClubRepository.findAll(spec, PageUtil.getSortedPageable(sortedPageable));

            return bookClubs.map(bookClub -> BookClubListResponse.builder()
                    .bookClubId(bookClub.getBookClubId())
                    .bookClubName(bookClub.getBookClubName())
                    .description(bookClub.getDescription())
                    .startDate(bookClub.getStartDate())
                    .endDate(bookClub.getEndDate())
                    .recruitmentStartDate(bookClub.getRecruitmentStartDate())
                    .recruitmentEndDate(bookClub.getRecruitmentEndDate())
                    .bookCover(getCurrentChallengeBookCover(bookClub.getBookClubId()))
                    .favoriteGenre(bookClub.getFavoriteGenre())
                    .build());
        }


        // 북클럽 세부 조회
        public BookClubResponse getBookClub(Long clubId) {
            BookClub bookClub = bookClubRepository.findById(clubId)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CLUB));

            List<BookClubChallenge> challengeList = bookClubChallengeRepository.findAllByDelYnAndBookClubId(false, bookClub.getBookClubId());
            List<BookClubChallengeResponse> challengeResponses = getChallengeResponses(challengeList);

            BookClubResponse bookClubResponse = new BookClubResponse();
            bookClubResponse.createBookClubResponse(bookClub, challengeResponses);
            return bookClubResponse;
        }

        public List<BookClubChallengeResponse> getChallengeResponses(List<BookClubChallenge> challenges) {

            return challenges.stream()
                    .map(challenge -> {
                        BookResponse book = bookService.getBookByIsbn(challenge.getIsbn13());
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
        public String getCurrentChallengeBookCover(Long bookClubId) {
            BookClubResponse bookClub = findById(bookClubId);
            List<BookClubChallenge> challenges = bookClubChallengeRepository.findAllByBookClubId(bookClubId);

            // 현재 활성화된 챌린지 찾기
            return challenges.stream()
                    .findFirst()
                    .map(challenge -> bookService.getBookByIsbn(challenge.getIsbn13()).getBookCover())
                    .orElse(null);
        }

        public void incrementViewCount(Long bookClubId) {
            BookClub bookClub = bookClubRepository.findById(bookClubId)
                    .orElseThrow(() -> new CustomException(INVALID_CLUB));
            bookClub.setViewCount(bookClub.getViewCount() + 1);

            bookClubRepository.save(bookClub);
        }

        public List<BookClub> getBookClubsOrderedByViewCount() {
            return bookClubRepository.findAll(Sort.by(Sort.Direction.DESC, "viewCount"));
        }


        public void isBookClubNameTaken(String clubName) {
            if (bookClubRepository.existsByBookClubName(clubName)) {
                throw new CustomException(ErrorCode.DUPLICATE_CLUB_NAME);
            }
        }

        //책 저장 메서드
        private List<Book> saveBooksByIsbn(List<BookInfoRequest> bookInfoRequests) {
            return bookInfoRequests.stream()
                    .map(bookInfo -> bookService.saveBookByIsbn(bookInfo.getIsbn()))
                    .toList();
        }

        public BookClubResponse findById(final long bookClubId) {
            BookClub bookClub = bookClubRepository.findById(bookClubId)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CLUB));

            // BookClub의 정보를 이용해 BookClubResponse를 생성
            return BookClubResponse.builder()
                    .bookClubId(bookClubId)
                    .bookClubName(bookClub.getBookClubName())
                    .description(bookClub.getDescription())
                    .startDate(bookClub.getStartDate())
                    .endDate(bookClub.getEndDate())
                    .recruitmentStartDate(bookClub.getRecruitmentStartDate())
                    .recruitmentEndDate(bookClub.getRecruitmentEndDate())
                    .viewCount(bookClub.getViewCount())
                    .favoriteGenre(bookClub.getFavoriteGenre())
                    .build();
        }
        public Page<BookClubListResponse> getBookClubByIsbn13(long bookId, Pageable pageable) {
            // 1. isbn13을 통해 챌린지 Repository 에 일치하는 챌린지 다 받아오기
            List<BookClubChallenge>  bookClubChallenges = bookClubChallengeRepository.findByIsbn13(bookId);

            if (bookClubChallenges.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_BOOK);
            }

            // 2. 받아온 챌린지 엔티티로 BookId 리스트생성
            List<Long> bookIds = bookClubChallenges.stream()
                    .map(BookClubChallenge::getBookClubId)
                    .toList();

            // 2. bookIds로 BookClub 조회 (페이징 처리)
            Page<BookClub> bookClubs = bookClubRepository.findByBookClubIdIn(bookIds, pageable);

            // 3. BookClub 리스트를 BookClubListResponse 리스트로 변환
            return bookClubs.map(bookClub ->
                    BookClubListResponse.builder()
                            .bookClubId(bookClub.getBookClubId())
                            .bookClubName(bookClub.getBookClubName())
                            .description(bookClub.getDescription())
                            .startDate(bookClub.getStartDate())
                            .endDate(bookClub.getEndDate())
                            .recruitmentStartDate(bookClub.getRecruitmentStartDate())
                            .recruitmentEndDate(bookClub.getRecruitmentEndDate())
                            .favoriteGenre(bookClub.getFavoriteGenre())
                            .build()
            );

        }

        public Page<BookClubListResponse> getBookClubByUserId(final long userId, Pageable pageable) {
            // 1. BookClubMember 에서 유저 아이디로 BookClubMember 조회
            List<Long> bookClubIds = bookClubMemberService.findBookClubIdsByUserId(userId);

            // 2. 책클럽 Id 로 북클럽 조회 - 최신순 정렬
            Pageable sortedPageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );

            // 3. 책클럽 Id로 북클럽 조회 (정렬 조건이 추가된 pageable 사용)
            Page<BookClub> bookClubs = bookClubRepository.findByBookClubIdIn(bookClubIds, PageUtil.getSortedPageable(pageable));

            // 3. BookClub 리스트를 BookClubListResponse 리스트로 변환
            return bookClubs.map(bookClub ->
                    BookClubListResponse.builder()
                            .bookClubId(bookClub.getBookClubId())
                            .bookClubName(bookClub.getBookClubName())
                            .description(bookClub.getDescription())
                            .startDate(bookClub.getStartDate())
                            .endDate(bookClub.getEndDate())
                            .recruitmentStartDate(bookClub.getRecruitmentStartDate())
                            .recruitmentEndDate(bookClub.getRecruitmentEndDate())
                            .favoriteGenre(bookClub.getFavoriteGenre())
                            .build()
            );
        }

    }





