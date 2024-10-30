package com.readmate.ReadMate.comment.repository;

import com.readmate.ReadMate.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository  extends JpaRepository<Comment, Long> {
    List<Comment> findByBoardId(Long boardId);

    //정렬
    List<Comment> findByBoardIdOrderByCreatedAtDesc(Long boardId);
    List<Comment> findByBoardIdOrderByCreatedAtAsc(Long boardId);

    Page<Comment> findByBoardId(Long boardId, Pageable pageable);

    int countByBoardId(Long boardId);
}
