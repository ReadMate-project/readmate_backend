package com.readmate.ReadMate.comment.controller;

import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.board.entity.BoardType;
import com.readmate.ReadMate.board.service.BoardService;
import com.readmate.ReadMate.bookclub.bookClubMember.dto.BookClubMemberResponse;
import com.readmate.ReadMate.bookclub.bookClubMember.service.BookClubMemberService;
import com.readmate.ReadMate.comment.dto.CommentRequest;
import com.readmate.ReadMate.comment.dto.CommentResWithPageInfo;
import com.readmate.ReadMate.comment.dto.CommentResponse;
import com.readmate.ReadMate.comment.entity.Comment;
import com.readmate.ReadMate.comment.service.CommentService;
import com.readmate.ReadMate.common.dto.PageInfo;
import com.readmate.ReadMate.common.exception.CustomException;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import com.readmate.ReadMate.common.message.BasicResponse;
import com.readmate.ReadMate.login.entity.User;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import com.readmate.ReadMate.login.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/comment")
@Tag(name = "comment", description = "comment API")
public class CommentController {

    private final CommentService commentService;
    private final BoardService boardService;
    private final BookClubMemberService bookClubMemberService;
    private final UserService userService;


    //1. 댓글 작성
    @PostMapping("/{boardId}")
    @Operation(summary = "댓글 작성", description = "댓글 작성 API")
    public ResponseEntity<BasicResponse<Comment>> createComment(
            @PathVariable("boardId") Long boardId,
            @RequestBody CommentRequest commentRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        if (userDetails == null || userDetails.getUser() == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Board board = boardService.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_BOARD));


        if (board.getBoardType() == BoardType.CLUB_BOARD) {

            Long bookclubId = board.getBookclubId();

            // 사용자가 해당 북클럽의 회원인지 확인
            List<BookClubMemberResponse> memberResponses = bookClubMemberService.findMembers(bookclubId, userDetails.getUser().getUserId(),false);
            if (memberResponses.isEmpty()) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
        }

        Comment comment = commentService.saveComment(commentRequest, userDetails.getUser().getUserId(), boardId);

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


    //4. 댓글 최신순 or 등록순으로 정렬
    @GetMapping("/{boardId}/comments")
    @Operation(summary = "댓글 목록 조회", description = "댓글 목록을 조회할 때 정렬 기준을 선택할 수 있습니다.")
    public ResponseEntity<BasicResponse<CommentResWithPageInfo>> getComments(
            @PathVariable("boardId") Long boardId,
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "sort", required = false, defaultValue = "latest") String sort) {


        Board board = boardService.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_BOARD));

        Pageable pageable = sort.equals("latest")
                ? PageRequest.of(page, size, Sort.by("createdAt").descending())
                : PageRequest.of(page, size, Sort.by("createdAt").ascending());

        Page<Comment> commentPage = commentService.findCommentsByBoardIdWithPagination(boardId, pageable);

        List<CommentResponse> commentResponses = commentPage.getContent().stream().map(comment -> {
            User user = userService.getUserById(comment.getUserId());
            return new CommentResponse(
                    comment.getCommentId(),
                    comment.getBoardId(),
                    comment.getUserId(),
                    comment.getContent(),
                    comment.getCreatedAt().toString(),
                    user.getNickname(),
                    user.getProfileImageUrl()
            );
        }).collect(Collectors.toList());

        PageInfo pageInfo = new PageInfo(
                commentPage.getNumber(),
                commentPage.getSize(),
                (int) commentPage.getTotalElements(),
                commentPage.getTotalPages()
        );

        CommentResWithPageInfo responseBody = new CommentResWithPageInfo(commentResponses, pageInfo);
        BasicResponse<CommentResWithPageInfo> response = BasicResponse.ofSuccess(responseBody);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}