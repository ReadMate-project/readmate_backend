package com.readmate.ReadMate.board.service;

import com.readmate.ReadMate.board.dto.response.CalendarBookResponse;
import com.readmate.ReadMate.board.dto.response.FeedResponse;
import com.readmate.ReadMate.board.dto.response.MVPResponse;
import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.board.entity.BoardType;
import com.readmate.ReadMate.board.repository.BoardRepository;
import com.readmate.ReadMate.book.dto.res.BookResponse;
import com.readmate.ReadMate.book.entity.Book;
import com.readmate.ReadMate.book.repository.BookRepository;
import com.readmate.ReadMate.book.service.BookService;
import com.readmate.ReadMate.book.service.MyBookService;
import com.readmate.ReadMate.bookclub.bookClubChallenge.entity.BookClubChallenge;
import com.readmate.ReadMate.bookclub.bookClubChallenge.repository.BookClubChallengeRepository;
import com.readmate.ReadMate.bookclub.dailyMission.entity.DailyMission;
import com.readmate.ReadMate.bookclub.dailyMission.repository.DailyMissionCompletionRepository;
import com.readmate.ReadMate.bookclub.dailyMission.repository.DailyMissionRepository;
import com.readmate.ReadMate.comment.repository.CommentRepository;
import com.readmate.ReadMate.common.exception.CustomException;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import com.readmate.ReadMate.like.repository.LikesRepository;
import com.readmate.ReadMate.login.entity.User;
import com.readmate.ReadMate.login.repository.UserRepository;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final MyBookService myBookService;
    private final BookService bookService;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final LikesRepository likesRepository;
    private final CommentRepository commentRepository;
    private final DailyMissionCompletionRepository dailyMissionCompletionRepository;
    private final BookClubChallengeRepository bookClubChallengeRepository;
    private final DailyMissionRepository dailyMissionRepository;


    //0.게시판 작성
    public Board saveBoard(CustomUserDetails user, Board board) {
        // 내 서재에 책 추가
        if (board.getBookId() != null) {
            // ISBN13을 사용해 도서 정보를 조회하고, 없으면 Aladin API로 저장
            Book saveBook = bookService.saveBookByIsbn(board.getBookId());

            // 해당 책을 사용자의 서재(MyBook)에 추가

            // bookId 로 저장되기 때문에, 이렇게하면 book_id 만 들어감.
            myBookService.saveMyBook(user, saveBook);
        }
        return boardRepository.save(board);
    }

    //1.게시판 수정
    public Optional<Board> findBoardById(Long boardId) {
        return boardRepository.findById(boardId);
    }

    @Transactional
    public boolean deleteBoard(Long boardId) {
        // 게시판 존재 여부 확인
        Optional<Board> board = boardRepository.findById(boardId);
        if (board.isEmpty()) {
            return false;
        }

        // 1. Board에서 BookClubId 조회
        Long bookClubId = board.get().getBookclubId();

        // 2. BookClubChallenge에서 ChallengeId 조회
        List<BookClubChallenge> bookClubChallenges = bookClubChallengeRepository.findAllByBookClubId(bookClubId);
        if (bookClubChallenges.isEmpty()) {
            boardRepository.deleteById(boardId); // Challenge가 없으면 Board만 삭제
            return true;
        }

        // 3. ChallengeId를 통해 DailyMission 가져오기
        for (BookClubChallenge bookClubChallenge : bookClubChallenges) {
            Long challengeId = bookClubChallenge.getChallengeId();

            // 4. ChallengeId를 사용해 DailyMission 가져오기
            List<DailyMission> dailyMissions = dailyMissionRepository.findAllByChallengeId(challengeId);
            for (DailyMission dailyMission : dailyMissions) {
                // 5. DailyMissionCompletion 삭제
                dailyMissionCompletionRepository.deleteByDailyMissionId(dailyMission.getMissionId());
            }
        }

        boardRepository.deleteById(boardId);

        return true;
    }


    //3. 게시판 별 내가 쓴 글 목록 조회
    public Page<Board> getBoardsByUserIdAndType(Long userId, BoardType boardType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return boardRepository.findByUserIdAndBoardType(userId, boardType, pageable);
    }

    //4. 게시판 별 게시글 목록 조회
    public Page<Board> getBoardsByType(BoardType boardType, int page, int size, Long bookclubId) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (boardType == BoardType.CLUB_BOARD) {
            return boardRepository.findByBookclubId(bookclubId, pageable); // bookclubId를 사용하여 클럽 게시글 반환
        }

        return boardRepository.findByBoardType(boardType, pageable); // 일반 게시글 반환
    }

    //5. 게시판 상세 조회
    public Board getBoardById(Long boardId) {
        return boardRepository.findById(boardId).orElse(null);
    }

    public Optional<Board> findById(Long boardId) {
        return boardRepository.findById(boardId);
    }

    //6.날자별 에세이 조회 (캘린더)
    public List<CalendarBookResponse> getBooksByMonth(final long userId, int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.toLocalDate().lengthOfMonth());

        //유저가 작성한 게시글 중에 해당 월의 피드 조회
        List<Board> boardList = boardRepository.findByUserIdAndCreatedAtBetweenAndBoardType(userId, startOfMonth, endOfMonth, BoardType.FEED);

        // 날짜별로 그룹화하여 책 정보를 저장할 맵 생성
        Map<String, List<CalendarBookResponse.BookInfo>> bookMapByDate = new HashMap<>();

        boardList.forEach(board -> {
            System.out.println(board.getBoardId() + "  : " + board.getBookId()); //null 이 들어감
            String date = board.getCreatedAt().toLocalDate().toString();
            Book book = bookRepository.findByIsbn13(board.getBookId())
                    .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND)); // 책 정보 가져오기

            CalendarBookResponse.BookInfo bookInfo = new CalendarBookResponse.BookInfo(book.getIsbn13(), book.getBookCover());

            // 날짜별로 책 정보를 그룹화하여 리스트에 추가
            bookMapByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(bookInfo);
        });
        return bookMapByDate.entrySet().stream()
                .map(entry -> new CalendarBookResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<FeedResponse> getFeedsByDate(LocalDateTime date) {
        // 해당 날짜의 시작 시간과 끝 시간을 정의
        LocalDateTime startOfDay = date.with(LocalTime.MIN);
        LocalDateTime endOfDay = date.with(LocalTime.MAX);

        // 시작 시간과 끝 시간 사이에 작성된 피드 목록 조회
        List<Board> boardList = boardRepository.findByCreatedAtBetweenAndBoardType(startOfDay, endOfDay, BoardType.FEED);

        // FeedResponse로 변환
        return boardList.stream().map(board -> {
            User user = userRepository.findByUserId(board.getUserId())
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER));

            int likeCount = likesRepository.countByBoardIdAndLikedTrue(board.getBoardId());
            int commentCount = commentRepository.countByBoardId(board.getBoardId());

            // BookResponse 생성 (ISBN 기반)
            BookResponse bookResponse = bookService.getBookByIsbn(board.getBookId());

            // FeedResponse 반환
            return new FeedResponse(
                    board.getBoardId(),
                    board.getTitle(),
                    board.getContent(),
                    board.getCreatedAt().toString(),
                    user.getUserId(),
                    user.getProfileImageUrl(),
                    user.getNickname(),
                    bookResponse,
                    likeCount,
                    commentCount
            );
        }).collect(Collectors.toList());
    }

    public List<MVPResponse> getMVPResponse(Long bookClubId) {

        LocalDateTime startOfWeek = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay();
        LocalDateTime endOfWeek = LocalDateTime.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toLocalDate().atTime(23, 59, 59);

        // 2. 해당 북클럽에 속한 이번 주에 생성된 피드(에세이) 조회
        List<Board> boards = boardRepository.findByBookclubIdAndBoardTypeAndCreatedAtBetween(bookClubId, BoardType.FEED, startOfWeek, endOfWeek);

        // 2. 각 피드(에세이)에 대해 좋아요, 댓글 수를 조회하고, MVPResponse 객체 생성
        List<MVPResponse> mvpResponses = boards.stream().map(board -> {

                    // 좋아요와 댓글 수 조회
                    int likeCount = likesRepository.countByBoardIdAndLikedTrue(board.getBoardId());
                    int commentCount = commentRepository.countByBoardId(board.getBoardId());

                    // 게시글 작성자의 유저 정보 가져오기
                    User user = userRepository.findById(board.getUserId())
                            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER));

                    // MVPResponse 객체 생성, 좋아요와 댓글 수 포함
                    return new MVPResponse(
                            board.getBoardId(),
                            board.getBookId(),
                            board.getTitle(),
                            board.getContent(),
                            user.getUserId(),
                            user.getNickname(),
                            user.getProfileImageUrl(),
                            likeCount,       // 좋아요 수
                            commentCount     // 댓글 수
                    );
                })
                // 3. 좋아요 수와 댓글 수 기준으로 내림차순 정렬
                .sorted((mvp1, mvp2) -> {
                    int result = Integer.compare(mvp2.getLikeCount(), mvp1.getLikeCount());
                    if (result == 0) {
                        result = Integer.compare(mvp2.getCommentCount(), mvp1.getCommentCount());
                    }
                    return result;
                })
                .collect(Collectors.toList());

        return mvpResponses;
    }


    //HOT-POST
    public Page<Board> getTopBoardsByLikesAndComments(BoardType boardType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Board> boards = boardRepository.findTopBoardsByLikesAndComments(boardType, pageable);
        return new PageImpl<>(boards, pageable, boards.size());
    }


    public List<FeedResponse> getFeedsByBookId(long bookId) {
        // board에서 해당하는 것 다 받아오기
        List<Board> boardList = boardRepository.findByBookId(bookId); // 해당 책 ID에 대한 모든 피드 조회

        // 최신순으로 정렬 (createdAt 기준 내림차순)
        boardList.sort((board1, board2) -> board2.getCreatedAt().compareTo(board1.getCreatedAt()));

        // Board -> FeedResponse로 변환
        return boardList.stream().map(board -> {
            // 유저 정보 조회
            User user = userRepository.findById(board.getUserId())
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER));

            // 책 정보를 BookService를 통해 가져오기
            BookResponse bookResponse = bookService.getBookByIsbn(board.getBookId());

            // 좋아요와 댓글 수 조회
            int likeCount = likesRepository.countByBoardIdAndLikedTrue(board.getBoardId());
            int commentCount = commentRepository.countByBoardId(board.getBoardId());

            // FeedResponse 생성
            return new FeedResponse(
                    board.getBoardId(),
                    board.getTitle(),
                    board.getContent(),
                    board.getCreatedAt().toString(),
                    user.getUserId(),
                    user.getProfileImageUrl(),
                    user.getNickname(),
                    bookResponse,
                    likeCount,
                    commentCount
            );
        }).collect(Collectors.toList());

    }
}
