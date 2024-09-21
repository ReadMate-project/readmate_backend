package com.readmate.ReadMate.board.service;

import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    //0.게시판 작성
    public Board saveBoard(Board board) {
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

    //3. 내가 쓴 글 목록 조회


}
