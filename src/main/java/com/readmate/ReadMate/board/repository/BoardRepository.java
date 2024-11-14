package com.readmate.ReadMate.board.repository;

import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.board.entity.BoardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    List<Board> findByBookclubIdAndBoardType(Long bookclubId, BoardType boardType);


    @Query("SELECT b FROM Board b LEFT JOIN Likes l ON b.BoardId = l.boardId AND l.liked = true " +
            "LEFT JOIN Comment c ON b.BoardId = c.boardId " +
            "WHERE b.boardType = :boardType " +
            "GROUP BY b.BoardId " +
            "ORDER BY COUNT(l.likeId) DESC, COUNT(c.commentId) DESC, b.createdAt ASC")
    List<Board> findTopBoardsByLikesAndComments(@Param("boardType") BoardType boardType, Pageable pageable);



}

