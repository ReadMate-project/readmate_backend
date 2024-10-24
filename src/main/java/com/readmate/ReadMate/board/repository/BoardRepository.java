package com.readmate.ReadMate.board.repository;

import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.board.entity.BoardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface BoardRepository  extends JpaRepository<Board, Long>, JpaSpecificationExecutor<Board> {

    Page<Board> findByUserIdAndBoardType(Long userId, BoardType boardType, Pageable pageable);
    Page<Board> findByBoardType(BoardType boardType, Pageable pageable);
    Page<Board> findByBookclubId(Long bookclubId, Pageable pageable);
    List<Board> findByCreatedAtBetween(LocalDateTime startOfMonth, LocalDateTime endOfMonth);

    List<Board> findByCreatedAtBetweenAndBoardType(LocalDateTime startOfDay, LocalDateTime endOfDay, BoardType boardType);
}

