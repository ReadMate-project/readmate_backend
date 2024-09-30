package com.readmate.ReadMate.comment.controller;

import com.readmate.ReadMate.board.dto.BoardRequest;
import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.comment.dto.CommentRequest;
import com.readmate.ReadMate.comment.entity.Comment;
import com.readmate.ReadMate.comment.service.CommentService;
import com.readmate.ReadMate.common.message.BasicResponse;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/comment")
@Tag(name = "comment", description = "comment API")
public class CommentController {

    private final CommentService commentService;

    //1. 댓글 작성
    @PostMapping("/{boardId}")
    @Operation(summary = "댓글 작성", description = "댓글 작성 API")
    public ResponseEntity<BasicResponse<Comment>> createComment(
            @PathVariable("boardId") Long boardId,
            @RequestBody CommentRequest commentRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            return new ResponseEntity<>(BasicResponse.ofError("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED.value()), HttpStatus.UNAUTHORIZED);
        }

        Comment comment = commentService.saveComment(commentRequest, userDetails.getUser().getUserId());

        BasicResponse<Comment> response = BasicResponse.ofSuccess(comment);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //2. 댓글 수정
    @PatchMapping("/{commentId}")
    @Operation(summary = "댓글 수정", description = "댓글 수정 API")
    public ResponseEntity<BasicResponse<Comment>> updateComment(
            @PathVariable("commentId") Long commentId,
            @RequestBody CommentRequest commentRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        if (userDetails == null || userDetails.getUser() == null) {
            return new ResponseEntity<>(BasicResponse.ofError("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED.value()), HttpStatus.UNAUTHORIZED);
        }

        Optional<Comment> optionalComment = commentService.findById(commentId);

        if (optionalComment.isPresent()) {
            Comment comment = optionalComment.get();

            // 인증된 사용자의 ID와 댓글의 userId 비교
            if (!comment.getUserId().equals(userDetails.getUser().getUserId())) {
                return new ResponseEntity<>(BasicResponse.ofError("해당 댓글에 대한 수정 권한이 없습니다.", HttpStatus.FORBIDDEN.value()), HttpStatus.FORBIDDEN);
            }

            if (commentRequest.getContent() != null) {
                comment.setContent(commentRequest.getContent());
            }
            comment.onUpdate();

            Comment updatedComment = commentService.updateComment(comment);
            BasicResponse<Comment> response = BasicResponse.ofSuccess(updatedComment);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(BasicResponse.ofError("해당 댓글이 존재하지 않습니다.", HttpStatus.NOT_FOUND.value()), HttpStatus.NOT_FOUND);
        }
    }

}
