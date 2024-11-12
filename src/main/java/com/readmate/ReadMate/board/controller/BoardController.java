package com.readmate.ReadMate.board.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readmate.ReadMate.board.dto.*;
import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.board.entity.BoardType;
import com.readmate.ReadMate.board.service.BoardService;
import com.readmate.ReadMate.bookclub.bookClubMember.dto.BookClubMemberResponse;
import com.readmate.ReadMate.bookclub.bookClubMember.entity.BookClubMember;
import com.readmate.ReadMate.bookclub.bookClubMember.entity.BookClubMemberRole;
import com.readmate.ReadMate.bookclub.bookClubMember.service.BookClubMemberService;
import com.readmate.ReadMate.bookclub.dailyMission.service.BookClubMissionService;
import com.readmate.ReadMate.common.dto.PageInfo;
import com.readmate.ReadMate.common.exception.CustomException;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import com.readmate.ReadMate.common.message.BasicResponse;
import com.readmate.ReadMate.common.message.ErrorResponse;
import com.readmate.ReadMate.image.service.ImageService;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/board")
@Tag(name = "board", description = "Board API")
public class BoardController {

    private final BoardService boardService;
    private final BookClubMemberService bookClubMemberService;
    private final ImageService imageService;
    private final BookClubMissionService bookClubMissionService;


    //0.게시판 작성
    //0-1. 일반 자유게시판일 경우 로그인 된 유저만 작성
    //0-2. 피드 -> 나만 작성할 수 있음 1.그냥 나만의 피드 작성 2. 챌린지용 피드가 존재
    //0-3. 북클럽 내 자유게시판 -> 북클럽 회원만 작성할 수 있음
    @PostMapping
    @Operation(summary = "게시물 작성", description = "게시물 작성 API")
    @Transactional // 문제 발생 시 롤백 위해
    public ResponseEntity<BasicResponse<Board>> createBoard(
            @RequestPart("boardRequest") String boardRequestJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            BoardRequest boardRequest = objectMapper.readValue(boardRequestJson, BoardRequest.class);

            if (userDetails == null || userDetails.getUser() == null) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }

            // 게시판에 따른 권한 체크
            BoardType boardType = boardRequest.getBoardType();
            BookClubMember member = null;

            switch (boardType) {
                case BOARD:
                    boardRequest.setBookId(null);
                    boardRequest.setBookclubId(null);
                    break;

                case FEED:
                    // 피드는 무조건 책이 선정되어야 함 -> 챌린지 인증을 위해
                    if (boardRequest.getBookId() == null) {
                        throw new CustomException(ErrorCode.INVALID_REQUEST);
                    }

                    // 챌린지 인증을 위한 북클럽 ID가 있는 경우
                    if (boardRequest.getBookclubId() != null) {
                        try {
                            // 북클럽 멤버 여부 확인
                            member = bookClubMemberService.findApprovedMember(boardRequest.getBookclubId(), userDetails.getUser().getUserId());
                        } catch (CustomException e) {
                            throw new CustomException(ErrorCode.FORBIDDEN);
                        }
                    }
                    break;

                case CLUB_BOARD:
                    try {
                        // 북클럽 멤버 여부 확인
                        System.out.println("User ID: " + userDetails.getUser().getUserId() + "BookClub ID:" + boardRequest.getBookclubId());

                        bookClubMemberService.findApprovedMember(boardRequest.getBookclubId(), userDetails.getUser().getUserId());
                        boardRequest.setBookId(null);
                    } catch (CustomException e) {
                        throw new CustomException(ErrorCode.FORBIDDEN);
                    }
                    break;

                case NOTICE:
                    // 공지사항 작성 시 북클럽 리더 확인!
                    if (boardRequest.getBookclubId() == null) {
                        throw new CustomException(ErrorCode.INVALID_REQUEST); // 북클럽 ID는 필수
                    }
                    validateLeader(boardRequest.getBookclubId(), userDetails);
                    break;

                default:
                    throw new CustomException(ErrorCode.INVALID_REQUEST);
            }

            Board board = new Board();
            board.setUserId(userDetails.getUser().getUserId());
//            board.setBookId(boardRequest.getBookId());
            board.setBookclubId(boardRequest.getBookclubId());
            board.setContent(boardRequest.getContent());
            board.setCreatedAt(LocalDateTime.now());
            board.setTitle(boardRequest.getTitle());
            board.setBoardType(boardType);

            // 게시물 저장
            Board savedBoard = boardService.saveBoard(userDetails, board);

            if (images != null && !images.isEmpty()) {
                try {
                    imageService.uploadImages(savedBoard.getBoardId(), images);
                } catch (IOException e) {
                    throw new CustomException(ErrorCode.FILE_UPLOAD_FAIL);
                }
            }

            // 챌린지 인증 미션 완료 처리 (bookclubId가 있는 경우)
            if (boardType == BoardType.FEED && boardRequest.getBookclubId() != null) {
                Long dailyMissionId = boardRequest.getDailyMissionId();
                bookClubMissionService.completeMission(dailyMissionId, userDetails.getUser().getUserId(), savedBoard.getBoardId());
            }

            BasicResponse<Board> response = BasicResponse.ofCreateSuccess(savedBoard);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }




    //1.게시판 수정
    // 1.게시판 수정
    @PatchMapping("/{boardId}")
    @Operation(summary = "게시물 수정", description = "게시물 수정 API")
    public ResponseEntity<BasicResponse<Board>> updateBoard(
            @PathVariable("boardId") Long boardId,
            @RequestPart(value = "updateRequest", required = false) String updateRequestJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            BoardUpdateRequest updateRequest = null;

            // JSON 문자열이 있을 경우에만 객체로 변환
            if (updateRequestJson != null && !updateRequestJson.isEmpty()) {
                updateRequest = objectMapper.readValue(updateRequestJson, BoardUpdateRequest.class);
            }

            if (userDetails == null || userDetails.getUser() == null) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }

            Optional<Board> optionalBoard = boardService.findBoardById(boardId);

            if (optionalBoard.isEmpty()) {
                throw new CustomException(ErrorCode.NOT_FOUND);
            }

            Board board = optionalBoard.get();

            if (!board.getUserId().equals(userDetails.getUser().getUserId())) {
                throw new CustomException(ErrorCode.FORBIDDEN);
            }

            if (updateRequest != null) {
                if (updateRequest.getBookclubId() != null) {
                    board.setBookclubId(updateRequest.getBookclubId());
                }
                if (updateRequest.getContent() != null) {
                    board.setContent(updateRequest.getContent());
                }
                if (updateRequest.getTitle() != null) {
                    board.setTitle(updateRequest.getTitle());
                }
            }

            if (images != null && !images.isEmpty()) {
                imageService.updateImages(board.getBoardId(), images);
            }

            Board updatedBoard = boardService.saveBoard(userDetails, board);
            BasicResponse<Board> response = BasicResponse.ofSuccess(updatedBoard);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAIL);
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

            //게시글에 해당하는 이미지들 삭제
            imageService.deleteImagesByBoardId(boardId);

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


        BasicResponse<List<Board>> response = BasicResponse.ofSuccessWithPageInfo(boardPage.getContent(), pageInfo);
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
            @RequestParam("bookclubId") Long bookclubId) { // 선택적 파라미터

        Page<Board> boardPage;

        if (boardType == BoardType.BOARD || boardType == BoardType.FEED) {
            // 로그인 유무와 상관없이 목록 조회 가능
            boardPage = boardService.getBoardsByType(boardType, page, size, null);
        } else if (boardType == BoardType.CLUB_BOARD) {
            // CLUB_BOARD는 로그인한 유저 + 해당 북클럽 회원만 조회 가능
            if (userDetails == null || userDetails.getUser() == null) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }

            Long userId = userDetails.getUser().getUserId();

            // 로그인한 유저가 해당 북클럽의 회원인지 확인
            List<BookClubMemberResponse> memberResponses = bookClubMemberService.findMembers(bookclubId, userDetails.getUser().getUserId(),false);

            // memberResponses를 사용해 북클럽 회원인지 확인
            if (memberResponses.isEmpty()) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }

            // 로그인한 유저가 소속된 북클럽의 게시물 조회 (bookclubId를 사용)
            boardPage = boardService.getBoardsByType(boardType, page, size, bookclubId); // bookclubId 사용

            // 게시물이 존재하지 않을 경우
            if (boardPage.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_BOARD);
            }
        } else {
            throw new CustomException(ErrorCode.INVALID_BOARD);
        }

        PageInfo pageInfo = new PageInfo(
                boardPage.getNumber(),
                boardPage.getSize(),
                (int) boardPage.getTotalElements(),
                boardPage.getTotalPages()
        );

        BasicResponse<List<Board>> response = BasicResponse.ofSuccessWithPageInfo(boardPage.getContent(), pageInfo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    //5. 게시판 상세 조회
    @GetMapping("/{boardId}")
    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 조회 API")
    public ResponseEntity<?> getBoardDetails(@PathVariable("boardId") Long boardId,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {

        Board board = boardService.getBoardById(boardId);

        if (board == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 게시물은 존재하지 않습니다.");
        }

        if (board.getBoardType() == BoardType.CLUB_BOARD) {
            if (userDetails == null || userDetails.getUser() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }

            // 해당 북클럽의 회원인지 확인
            try { // memberResponses가 비어있지 않은 경우는 이미 회원인 경우이므로 아무 행동도 하지 않음
                 bookClubMemberService.findMembers(board.getBookclubId(), userDetails.getUser().getUserId(),false);
            } catch (CustomException e) {
                if (e.getErrorCode() == ErrorCode.NOT_MEMBER) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("이 게시물은 북클럽 회원만 볼 수 있습니다.");
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("오류가 발생했습니다.");
            }
        }

        return ResponseEntity.ok(board);
    }

    // 날짜 범위 내에서 작성된 피드에 해당하는 책 정보를 반환
    @GetMapping("/calendar/books")
    @Operation(summary = "캘린더에 표시할 책 정보 조회", description = "특정 날짜 범위 내에서 작성된 피드에 해당하는 책 정보를 반환합니다.")
    public ResponseEntity<BasicResponse<List<CalendarBookResponse>>> getBooksForCalendar(
            @RequestParam("year") int year,
            @RequestParam("month") int month) {

        // 날짜 범위에 해당하는 책 정보를 가져옴
        List<CalendarBookResponse> books = boardService.getBooksByMonth(year, month);

        BasicResponse<List<CalendarBookResponse>> response = BasicResponse.ofSuccess(books);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/feeds/{date}")
    @Operation(summary = "날짜별 피드 조회", description = "선택한 날짜에 작성된 피드 목록 조회 API")
    public ResponseEntity<BasicResponse<List<FeedResponse>>> getFeedsByDate(
            @PathVariable String date,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 날짜 형식 체크
        LocalDateTime selectedDate;
        try {
            selectedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INVALID_BOOK_DATE);
        }

        // 해당 날짜의 피드 목록 조회
        List<FeedResponse> feedResponses = boardService.getFeedsByDate(selectedDate);

        // 피드가 없는 경우
        if (feedResponses.isEmpty()) {
            throw new CustomException(ErrorCode.NO_FEEDS_FOUND);
        }

        return ResponseEntity.ok(BasicResponse.ofSuccess(feedResponses));
    }

    // 북클럽 리더인지 확인하는 메서드 -> 공지사항땜에 필요
    private void validateLeader(Long bookclubId, CustomUserDetails userDetails) {
        List<BookClubMemberResponse> members = bookClubMemberService.findMembers(bookclubId, userDetails.getUser().getUserId(),false);
        boolean isLeader = members.stream()
                .anyMatch(member -> member.getUserId().equals(userDetails.getUser().getUserId()) &&
                        member.getClubMemberRole().equals(BookClubMemberRole.LEADER));

        if (!isLeader) {
            throw new CustomException(ErrorCode.NOT_LEADER);
        }
    }
}