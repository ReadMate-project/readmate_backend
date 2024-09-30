package com.readmate.ReadMate.board.controller;

import com.readmate.ReadMate.board.dto.BoardRequest;
import com.readmate.ReadMate.board.dto.BoardUpdateRequest;
import com.readmate.ReadMate.board.dto.PageInfo;
import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.board.entity.BoardType;
import com.readmate.ReadMate.board.repository.BoardRepository;
import com.readmate.ReadMate.board.service.BoardService;
import com.readmate.ReadMate.bookclub.dto.res.BookClubMemberResponse;
import com.readmate.ReadMate.bookclub.service.BookClubMemberService;
import com.readmate.ReadMate.common.exception.CustomException;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import com.readmate.ReadMate.common.message.BasicResponse;
import com.readmate.ReadMate.common.message.ErrorResponse;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import com.readmate.ReadMate.login.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/board")
@Tag(name = "board", description = "Board API")
public class BoardController {

    private final BoardService boardService;
    private final BookClubMemberService bookClubMemberService;


    //0.게시판 작성
    //0-1. 일반 자유게시판일 경우 로그인 된 유저만 작성
    //0-2. 피드 -> 나만 작성할 수 있음 
    //0-3. 북클럽 내 자유게시판 -> 북클럽 회원만 작성할 수 있음

    @PostMapping
    @Operation(summary = "게시물 작성", description = "게시물 작성 API")
    public ResponseEntity<BasicResponse<Board>> createBoard(@RequestBody BoardRequest boardRequest,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null || userDetails.getUser() == null) { //해당 오류가 발생하면 로그인화면으로 redirect

            BasicResponse<Board> errorResponseWrapper = BasicResponse.ofError("로그인을 진행해주세요", HttpStatus.UNAUTHORIZED.value());
            return new ResponseEntity<>(errorResponseWrapper, HttpStatus.UNAUTHORIZED);
        }


        //게시판에 따른 권한 체크
        BoardType boardType = boardRequest.getBoardType();

        switch (boardType) {
            case BOARD:
                boardRequest.setBookId(null);
                boardRequest.setBookclubId(null);
                break;

            case FEED:
                //피드는 무조건적으로 책을 선정해야한다. -> 챌린지 인증을 위해
                if (boardRequest.getBookId() == null) {
                    BasicResponse<Board> errorResponseWrapper = BasicResponse.ofError(
                            "피드에는 bookId가 필수입니다.", HttpStatus.BAD_REQUEST.value());
                    return new ResponseEntity<>(errorResponseWrapper, HttpStatus.BAD_REQUEST);
                }
                break;

            case CLUB_BOARD:
                if (userDetails == null || userDetails.getUser() == null) {
                    BasicResponse<Board> errorResponseWrapper = BasicResponse.ofError("인증된 유저가 아닙니다.", HttpStatus.UNAUTHORIZED.value());
                    return new ResponseEntity<>(errorResponseWrapper, HttpStatus.UNAUTHORIZED);
                }

                try {
                    // 북클럽 멤버 여부 확인만 수행 (리턴값 사용하지 않음)
                    bookClubMemberService.findMember(boardRequest.getBookclubId(), userDetails);

                    boardRequest.setBookId(null);
                } catch (CustomException e) {
                    BasicResponse<Board> errorResponseWrapper = BasicResponse.ofError("해당 북클럽의 회원이 아닙니다.", HttpStatus.FORBIDDEN.value());
                    return new ResponseEntity<>(errorResponseWrapper, HttpStatus.FORBIDDEN);
                }
                break;

            default:
                BasicResponse<Board> errorResponseWrapper = BasicResponse.ofError("잘못된 게시판 타입입니다.", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(errorResponseWrapper, HttpStatus.BAD_REQUEST);
        }


        Board board = new Board();
        board.setUserId(boardRequest.getUserId());
        board.setBookId(boardRequest.getBookId()); //BOARD와 CLUB_BOARD의 경우 null로 설정됨
        board.setBookclubId(boardRequest.getBookclubId()); //BOARD같은 경우 null로 설정 (자유게시판이니)
        board.setContent(boardRequest.getContent());
        board.setCreatedAt(LocalDateTime.now());
        board.setTitle(boardRequest.getTitle());
        board.setBoardType(boardType);

        Board savedBoard = boardService.saveBoard(board);
        BasicResponse<Board> response = BasicResponse.ofCreateSuccess(savedBoard);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    //1.게시판 수정
    @PatchMapping("/{boardId}")
    @Operation(summary = "게시물 수정", description = "게시물 수정 API")
    public ResponseEntity<BasicResponse<Board>> updateBoard(
            @PathVariable("boardId") Long boardId,
            @RequestBody BoardUpdateRequest updateRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null || userDetails.getUser() == null) {
            BasicResponse<Board> errorResponseWrapper = BasicResponse.ofError("로그인을 진행해주세요", HttpStatus.UNAUTHORIZED.value());
            return new ResponseEntity<>(errorResponseWrapper, HttpStatus.UNAUTHORIZED);
        }

        Optional<Board> optionalBoard = boardService.findBoardById(boardId);

        if (optionalBoard.isPresent()) {

            Board board = optionalBoard.get(); //해당 게시물을 가지고옴

            if (!board.getUserId().equals(userDetails.getUser().getUserId())) {
                BasicResponse<Board> errorResponseWrapper = BasicResponse.ofError("해당 게시물에 대한 수정 권한이 없습니다.", HttpStatus.FORBIDDEN.value());
                return new ResponseEntity<>(errorResponseWrapper, HttpStatus.FORBIDDEN);
            }

            if (updateRequest.getBookId() != null) {
                board.setBookId(updateRequest.getBookId());
            }
            if (updateRequest.getBookclubId() != null) {
                board.setBookclubId(updateRequest.getBookclubId());
            }
            if (updateRequest.getContent() != null) {
                board.setContent(updateRequest.getContent());
            }
            if (updateRequest.getTitle() != null) {
                board.setTitle(updateRequest.getTitle());
            }
            board.setCreatedAt(LocalDateTime.now());

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
    public ResponseEntity<?> deleteBoard(@PathVariable("boardId") Long boardId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null || userDetails.getUser() == null) {
            BasicResponse<String> errorResponseWrapper = BasicResponse.ofError("로그인을 진행해주세요", HttpStatus.UNAUTHORIZED.value());
            return new ResponseEntity<>(errorResponseWrapper, HttpStatus.UNAUTHORIZED);
        }


        Optional<Board> optionalBoard = boardService.findBoardById(boardId);

        if (optionalBoard.isPresent()) {
            Board board = optionalBoard.get();

            //게시물 작성자와 현재 인증된 사용자(=로그인한 유저) 비교 -> 작성자만이 삭제할 수 있는거니까
            if (!board.getUserId().equals(userDetails.getUser().getUserId())) {
                BasicResponse<String> errorResponseWrapper = BasicResponse.ofError("해당 게시물에 대한 삭제 권한이 없습니다.", HttpStatus.FORBIDDEN.value());
                return new ResponseEntity<>(errorResponseWrapper, HttpStatus.FORBIDDEN);
            }


            boolean isDeleted = boardService.deleteBoard(boardId);


            if (isDeleted) {
                BasicResponse<String> response = BasicResponse.ofSuccess("게시물이 삭제되었습니다.");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ErrorResponse errorResponse = new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_SERVER_ERROR", "게시물 삭제에 실패하였습니다.");
                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } else {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(), "NOT_FOUND", "게시물이 존재하지 않습니다.");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    //3. 게시판 별 내가 쓴 글 목록 조회
    @GetMapping("/mypost")
    @Operation(summary = "사용자 게시글 목록 조회", description = "특정 사용자와 게시글 타입에 따른 게시글 목록 조회 API")
    public ResponseEntity<BasicResponse<List<Board>>> getBoardsByUserIdAndType(
            @RequestParam("boardType") BoardType boardType,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("page") int page,
            @RequestParam("size") int size) {

        if (userDetails == null || userDetails.getUser() == null) {
            BasicResponse<List<Board>> errorResponseWrapper = BasicResponse.ofError("로그인을 진행해주세요", HttpStatus.UNAUTHORIZED.value());
            return new ResponseEntity<>(errorResponseWrapper, HttpStatus.UNAUTHORIZED);
        }

        // userId와 BoardType에 따른 게시글 목록 조회
        Long userId = userDetails.getUser().getUserId();
        Page<Board> boardPage = boardService.getBoardsByUserIdAndType(userId, boardType, page, size);

        PageInfo pageInfo = new PageInfo(
                boardPage.getNumber(),
                boardPage.getSize(),
                (int) boardPage.getTotalElements(),
                boardPage.getTotalPages()
        );

        BasicResponse<List<Board>> response = BasicResponse.ofSuccess(boardPage.getContent(), pageInfo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //4. 게시판 별 게시글 목록 조회
    //4-1. 자유 게시판일 경우에는 로그인 유무와 상관없이 목록 조회 가능
    //4-2. 북클럽내 자유게시판일 경우 로그인 및 해당 북클럽 회원이어야지만 해당 북클럽 자유게시판 목록 조회 가능
    @GetMapping("/")
    @Operation(summary = "게시글 목록 조회", description = "게시글 타입에 따른 게시글 목록 조회 API")
    public ResponseEntity<BasicResponse<List<Board>>> getBoardsByType(
            @RequestParam("boardType") BoardType boardType,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("bookclubId") Long bookclubId) {

        Page<Board> boardPage;

        if (boardType == BoardType.BOARD || boardType == BoardType.FEED) {
            // 로그인 유무와 상관없이 목록 조회 가능
            boardPage = boardService.getBoardsByType(boardType, page, size);
        }
        else if (boardType == BoardType.CLUB_BOARD) {
            // CLUB_BOARD는 로그인한 유저 + 해당 북클럽 회원만 조회 가능
            if (userDetails == null || userDetails.getUser() == null) {
                throw new CustomException(ErrorCode.UNAUTHORIZED); // UNAUTHORIZED 오류 발생
            }

            Long userId = userDetails.getUser().getUserId();

            // 로그인한 유저가 해당 북클럽의 회원인지 확인
            List<BookClubMemberResponse> memberResponses = bookClubMemberService.findMember(bookclubId, userDetails);

            // memberResponses를 사용해 북클럽 회원인지 확인
            if (memberResponses.isEmpty()) {
                throw new CustomException(ErrorCode.UNAUTHORIZED); // UNAUTHORIZED 오류 발생
            }

            // 로그인한 유저가 소속된 북클럽의 게시물 조회
            boardPage = boardService.getBoardsByUserIdAndType(userId, boardType, page, size);

            // 게시물이 존재하지 않을 경우
            if (boardPage.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_BOARD); // 존재하지 않는 게시물 오류 발생
            }
        }
        else {
            throw new CustomException(ErrorCode.INVALID_BOARD); // 잘못된 게시판 타입 오류 발생
        }

        PageInfo pageInfo = new PageInfo(
                boardPage.getNumber(),
                boardPage.getSize(),
                (int) boardPage.getTotalElements(),
                boardPage.getTotalPages()
        );

        BasicResponse<List<Board>> response = BasicResponse.ofSuccess(boardPage.getContent(), pageInfo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    //5. 게시판 상세 조회
    @GetMapping("/{boardId}")
    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 조회 API")
    public ResponseEntity<?> getBoardDetails(@PathVariable("boardId") Long boardId,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 게시글 조회
        Board board = boardService.getBoardById(boardId);

        // 게시글이 존재하지 않는 경우
        if (board == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 게시물은 존재하지 않습니다.");
        }

        // 게시판 타입이 CLUB_BOARD인 경우
        if (board.getBoardType() == BoardType.CLUB_BOARD) {
            // 인증된 유저인지 확인
            if (userDetails == null || userDetails.getUser() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }

            // 해당 북클럽의 회원인지 확인
            try { // memberResponses가 비어있지 않은 경우는 이미 회원인 경우이므로 아무 행동도 하지 않음
                 bookClubMemberService.findMember(board.getBookclubId(), userDetails);
            } catch (CustomException e) {
                if (e.getErrorCode() == ErrorCode.NOT_MEMBER) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("이 게시물은 북클럽 회원만 볼 수 있습니다.");
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("오류가 발생했습니다.");
            }
        }

        return ResponseEntity.ok(board);
    }

}