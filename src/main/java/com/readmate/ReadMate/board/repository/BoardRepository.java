package com.readmate.ReadMate.board.repository;

import com.readmate.ReadMate.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository  extends JpaRepository<Board, Long> {
}
