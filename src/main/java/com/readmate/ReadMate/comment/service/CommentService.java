package com.readmate.ReadMate.comment.service;

import com.readmate.ReadMate.bookclub.bookClubMember.service.BookClubMemberService;
import com.readmate.ReadMate.comment.dto.CommentRequest;
import com.readmate.ReadMate.comment.entity.Comment;
import com.readmate.ReadMate.comment.repository.CommentRepository;
import com.readmate.ReadMate.common.exception.CustomException;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BookClubMemberService bookClubMemberService;


    //1. 댓글 작성
    @Transactional
    public Comment saveComment(CommentRequest commentRequest, Long userId, Long boardId) {
        Comment comment = new Comment();
        comment.setBoardId(boardId);
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


    //3. 댓글 삭제
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        commentRepository.delete(comment);
    }
    

    //4. 댓글 목록 조회
    public List<Comment> findCommentsByBoardId(Long boardId, String sort) {
        if ("registered".equals(sort)) {
            return commentRepository.findByBoardIdOrderByCreatedAtAsc(boardId);
        } else {
            return commentRepository.findByBoardIdOrderByCreatedAtDesc(boardId);
        }
    }
    public Page<Comment> findCommentsByBoardIdWithPagination(Long boardId, Pageable pageable) {
        return commentRepository.findByBoardId(boardId, pageable);
    }
    
    //5. 게시글에 해당하는 댓글 총 개수
    public int countCommentsByBoardId(Long boardId) {
        return commentRepository.countByBoardId(boardId);
    }


}
