package com.readmate.ReadMate.board.service;

import com.readmate.ReadMate.board.dto.CalendarBookResponse;
import com.readmate.ReadMate.board.dto.FeedResponse;
import com.readmate.ReadMate.board.dto.MVPResponse;
import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.board.entity.BoardType;
import com.readmate.ReadMate.board.repository.BoardRepository;
import com.readmate.ReadMate.book.dto.res.BookResponse;
import com.readmate.ReadMate.book.entity.Book;
import com.readmate.ReadMate.book.repository.BookRepository;
import com.readmate.ReadMate.book.service.BookService;
import com.readmate.ReadMate.book.service.MyBookService;
import com.readmate.ReadMate.bookclub.bookClubMember.service.BookClubMemberService;
import com.readmate.ReadMate.comment.repository.CommentRepository;
import com.readmate.ReadMate.common.exception.CustomException;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import com.readmate.ReadMate.like.repository.LikesRepository;
import com.readmate.ReadMate.login.entity.User;
import com.readmate.ReadMate.login.repository.UserRepository;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import com.readmate.ReadMate.login.security.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
    //0.게시판 작성
    public Board saveBoard(CustomUserDetails user,Board board) {
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

    //2.게시판 삭제
    public boolean deleteBoard(Long boardId) {
        if (boardRepository.existsById(boardId)) {
            boardRepository.deleteById(boardId);
            return true;
        } else {
            return false;
        }
    }

    //3. 게시판 별 내가 쓴 글 목록 조회
    public Page<Board> getBoardsByUserIdAndType(Long userId, BoardType boardType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return boardRepository.findByUserIdAndBoardType(userId, boardType, pageable);
    }


    //4. 게시판 별 게시글 목록 조회
    public Page<Board> getBoardsByType(BoardType boardType, int page, int size, Long bookclubId) {
        Pageable pageable = PageRequest.of(page, size);

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
        return boardRepository.findById(boardId); // 게시글 조회
    }

    //6.날자별 에세이 조회 (캘린더)
    public List<CalendarBookResponse> getBooksByMonth(int year ,int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.toLocalDate().lengthOfMonth());

        List<Board> boardList = boardRepository.findByCreatedAtBetween(startOfMonth, endOfMonth); // 해당 월의 피드 조회

        // 날짜별로 그룹화하여 책 정보를 저장할 맵 생성
        Map<String, List<CalendarBookResponse.BookInfo>> bookMapByDate = new HashMap<>();

        boardList.forEach(board -> {
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
                    bookResponse
            );
        }).collect(Collectors.toList());
    }

    public List<MVPResponse> getMVPResponse(Long bookClubId) {

        // 1. 해당 북클럽에 속한 피드(에세이) 조회
        List<Board> boards = boardRepository.findByBookclubIdAndBoardType(bookClubId, BoardType.FEED);

        // 2. 각 피드(에세이)에 대해 좋아요, 댓글 수를 조회하고, MVPResponse 객체 생성
        List<MVPResponse> mvpResponses = boards.stream().map(board -> {

                    // 좋아요와 댓글 수 조회
                    int likeCount = likesRepository.countByBoardId(board.getBoardId());
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

}
