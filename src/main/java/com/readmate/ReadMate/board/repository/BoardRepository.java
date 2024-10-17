package com.readmate.ReadMate.board.repository;

import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.board.entity.BoardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BoardRepository  extends JpaRepository<Board, Long> {

    Page<Board> findByUserIdAndBoardType(Long userId, BoardType boardType, Pageable pageable);
    Page<Board> findByBoardType(BoardType boardType, Pageable pageable);
    Page<Board> findByBookclubId(Long bookclubId, Pageable pageable);
}
