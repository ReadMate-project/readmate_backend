package com.readmate.ReadMate.comment.service;

import com.readmate.ReadMate.comment.dto.CommentRequest;
import com.readmate.ReadMate.comment.entity.Comment;
import com.readmate.ReadMate.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;


    //1. 댓글 작성
    @Transactional
    public Comment saveComment(CommentRequest commentRequest, Long userId) {
        Comment comment = new Comment();
        comment.setBoardId(commentRequest.getBoardId());
        comment.setUserId(userId);
        comment.setContent(commentRequest.getContent());
        return commentRepository.save(comment);
    }



    //2.댓글 수정
    // 댓글 ID로 조회
    @Transactional
    public Comment updateComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public Optional<Comment> findById(Long commentId) {
        return commentRepository.findById(commentId);
    }



    // 특정 게시물에 대한 댓글 조회
    public List<Comment> getCommentsByBoardId(Long boardId) {
        return commentRepository.findByBoardId(boardId);
    }


}
