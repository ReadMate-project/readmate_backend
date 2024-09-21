package com.readmate.ReadMate.board.controller;

import com.readmate.ReadMate.board.dto.BoardRequest;
import com.readmate.ReadMate.board.dto.BoardUpdateRequest;
import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.board.service.BoardService;
import com.readmate.ReadMate.common.message.BasicResponse;
import com.readmate.ReadMate.common.message.ErrorResponse;
import com.readmate.ReadMate.login.entity.User;
import com.readmate.ReadMate.login.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/board")
@Tag(name = "board", description = "Board API")
public class BoardController {

    private final BoardService boardService;
    private final UserService userService;

    //0.게시판 작성
    @PostMapping
    @Operation(summary = "게시물 작성", description = "게시물 작성 API")
    public ResponseEntity<BasicResponse<Board>> createBoard(@RequestBody BoardRequest boardRequest) {

        Optional<User> optionalUser = userService.findUserById(boardRequest.getUserId());

        if (optionalUser.isEmpty()) {
            BasicResponse<Board> errorResponseWrapper = BasicResponse.ofError("해당 유저가 존재하지 않습니다.", HttpStatus.NOT_FOUND.value());
            return new ResponseEntity<>(errorResponseWrapper, HttpStatus.NOT_FOUND);
        }

        Board board = new Board();
        board.setUserId(boardRequest.getUserId());
        board.setBookId(boardRequest.getBookId());
        board.setBookclubId(boardRequest.getBookclubId());
        board.setTotalPages(boardRequest.getTotalPages());
        board.setCurrentPage(boardRequest.getCurrentPage());
        board.setContent(boardRequest.getContent());
        board.setCreatedAt(LocalDateTime.now());
        board.setTitle(boardRequest.getTitle());
        board.setLikes(0L);

        Board savedBoard = boardService.saveBoard(board);
        BasicResponse<Board> response = BasicResponse.ofCreateSuccess(savedBoard);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }



    //1.게시판 수정
    @PatchMapping("/{boardId}")
    @Operation(summary = "게시물 수정", description = "게시물 수정 API")
    public ResponseEntity<BasicResponse<Board>> updateBoard(
            @PathVariable("boardId") Long boardId,
            @RequestBody BoardUpdateRequest updateRequest) {

        Optional<Board> optionalBoard = boardService.findBoardById(boardId);

        if (optionalBoard.isPresent()) {
            Board board = optionalBoard.get();

            if (updateRequest.getBookId() != null) {
                board.setBookId(updateRequest.getBookId());
            }
            if (updateRequest.getBookclubId() != null) {
                board.setBookclubId(updateRequest.getBookclubId());
            }
            if (updateRequest.getTotalPages() != null) {
                board.setTotalPages(updateRequest.getTotalPages());
            }
            if (updateRequest.getCurrentPage() != null) {
                board.setCurrentPage(updateRequest.getCurrentPage());
            }
            if (updateRequest.getContent() != null) {
                board.setContent(updateRequest.getContent());
            }
            if (updateRequest.getTitle() != null) {
                board.setTitle(updateRequest.getTitle());
            }

            Board updatedBoard = boardService.saveBoard(board);
            BasicResponse<Board> response = BasicResponse.ofSuccess(updatedBoard);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } else {
            BasicResponse<Board> errorResponseWrapper = BasicResponse.ofError("게시물이 존재하지 않습니다.", HttpStatus.NOT_FOUND.value());
            return new ResponseEntity<>(errorResponseWrapper, HttpStatus.NOT_FOUND);
        }
    }


    //2.게시판 삭제
    @DeleteMapping("/{boardId}")
    @Operation(summary = "게시물 삭제", description = "게시물 삭제 API")
    public ResponseEntity<?> deleteBoard(@PathVariable("boardId") Long boardId) {

        boolean isDeleted = boardService.deleteBoard(boardId);

        if (isDeleted) {
            BasicResponse<String> response = BasicResponse.ofSuccess("게시물이 삭제되었습니다.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(), "NOT_FOUND", "게시물이 존재하지 않습니다.");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    //3. 내가 쓴 글 목록 조회
}
