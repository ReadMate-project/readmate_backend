package com.readmate.ReadMate.board.service;

import com.readmate.ReadMate.board.dto.BoardRequest;
import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.board.entity.BoardType;
import com.readmate.ReadMate.board.repository.BoardRepository;
import com.readmate.ReadMate.book.entity.Book;
import com.readmate.ReadMate.book.repository.BookRepository;
import com.readmate.ReadMate.book.service.BookService;
import com.readmate.ReadMate.book.service.MyBookService;
import com.readmate.ReadMate.bookclub.service.BookClubMemberService;
import com.readmate.ReadMate.common.exception.CustomException;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import com.readmate.ReadMate.login.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BookClubMemberService bookClubMemberService;
    private final MyBookService myBookService;
    private final BookService bookService;
    private final CustomUserDetailsService userDetailsService;

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

}
