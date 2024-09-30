package com.readmate.ReadMate.comment.controller;

import com.readmate.ReadMate.board.dto.BoardRequest;
import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.board.entity.BoardType;
import com.readmate.ReadMate.board.service.BoardService;
import com.readmate.ReadMate.bookclub.dto.res.BookClubMemberResponse;
import com.readmate.ReadMate.bookclub.service.BookClubMemberService;
import com.readmate.ReadMate.comment.dto.CommentRequest;
import com.readmate.ReadMate.comment.entity.Comment;
import com.readmate.ReadMate.comment.service.CommentService;
import com.readmate.ReadMate.common.exception.CustomException;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import com.readmate.ReadMate.common.message.BasicResponse;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/comment")
@Tag(name = "comment", description = "comment API")
public class CommentController {

    private final CommentService commentService;
    private final BoardService boardService;
    private final BookClubMemberService bookClubMemberService;


    //1. 댓글 작성
    @PostMapping("/{boardId}")
    @Operation(summary = "댓글 작성", description = "댓글 작성 API")
    public ResponseEntity<BasicResponse<Comment>> createComment(
            @PathVariable("boardId") Long boardId,
            @RequestBody CommentRequest commentRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 사용자 인증 확인
        if (userDetails == null || userDetails.getUser() == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        //해당 게시판의 타입을 가져오기 위해 boardId로 게시글 조회
        Board board = boardService.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_BOARD));


        if (board.getBoardType() == BoardType.CLUB_BOARD) {

            Long bookclubId = board.getBookclubId();

            // 사용자가 해당 북클럽의 회원인지 확인
            List<BookClubMemberResponse> memberResponses = bookClubMemberService.findMember(bookclubId, userDetails);
            if (memberResponses.isEmpty()) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
        }

        // 댓글 저장
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
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Comment comment = commentService.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        // 인증된 사용자의 ID와 댓글의 userId 비교
        if (!comment.getUserId().equals(userDetails.getUser().getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        if (commentRequest.getContent() != null) {
            comment.setContent(commentRequest.getContent());
        }
        comment.onUpdate();

        Comment updatedComment = commentService.updateComment(comment);
        BasicResponse<Comment> response = BasicResponse.ofSuccess(updatedComment);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    //3. 댓글 삭제
    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", description = "댓글 삭제 API")
    public ResponseEntity<BasicResponse<Void>> deleteComment(
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        commentService.deleteComment(commentId, userDetails.getUser().getUserId());
        BasicResponse<Void> response = BasicResponse.ofSuccess(null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
