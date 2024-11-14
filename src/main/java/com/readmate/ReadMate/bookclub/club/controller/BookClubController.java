package com.readmate.ReadMate.bookclub.club.controller;

import com.readmate.ReadMate.board.dto.response.BoardResponse;
import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.board.entity.BoardType;
import com.readmate.ReadMate.bookclub.club.dto.req.BookClubRequest;
import com.readmate.ReadMate.bookclub.club.dto.res.BookClubListResponse;
import com.readmate.ReadMate.bookclub.club.dto.res.BookClubResponse;
import com.readmate.ReadMate.bookclub.club.service.BookClubService;
import com.readmate.ReadMate.common.dto.PageInfo;
import com.readmate.ReadMate.common.message.BasicResponse;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookClub")
@Tag(name = "BookClub", description = "BookClub API")
public class BookClubController {


    private final BookClubService bookClubService;

    @PostMapping
    @Operation(summary = "북클럽 생성하기", description = "새로운 북클럽을 생성합니다")
    public ResponseEntity<?> createClub(@AuthenticationPrincipal final CustomUserDetails userDetails, @Valid @RequestBody BookClubRequest clubRequest) {
        return ResponseEntity.ok(BasicResponse.ofCreateSuccess(bookClubService.createClub(userDetails, clubRequest)));
    }

    @PatchMapping("/{bookClubId}")
    @Operation(summary = "북클럽 수정하기", description = "북클럽을 수정합니다")
    public ResponseEntity<?> updateClub(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable long bookClubId, @Valid @RequestBody BookClubRequest clubRequest) {
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubService.updateClub(userDetails, bookClubId, clubRequest)));
    }

    @DeleteMapping("/{bookClubId}")
    @Operation(summary = "북클럽 삭제하기", description = "북클럽을 삭제합니다")
    public ResponseEntity<?> deleteClub(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable long bookClubId) {
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubService.deleteClub(userDetails, bookClubId)));
    }

    @GetMapping()
    @Operation(summary = "북클럽 리스트 조회", description = "북클럽 리스트 조회합니다. 기본 최신순")
    public  ResponseEntity<?> getClubList(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "5") int size,
                                          @RequestParam(value="status", required = false,defaultValue = "전체")String status) {

        Pageable pageable = PageRequest.of(page-1, size);

        Page<BookClubListResponse> bookClubPage = bookClubService.getClubList(status, pageable);
        PageInfo pageInfo = new PageInfo(
                bookClubPage.getNumber(),
                bookClubPage.getSize(),
                (int) bookClubPage.getTotalElements(),
                bookClubPage.getTotalPages()
        );
        BasicResponse<List<BookClubListResponse>> response = BasicResponse.ofSuccessWithPageInfo(bookClubPage.getContent(), pageInfo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{bookClubId}")
    @Operation(summary = "북클럽 상세 조회", description = "북클럽 ID 로 북클럽 상세 조회합니다")
    public ResponseEntity<?> getBookClub(@PathVariable(name = "bookClubId") final long bookClubId) {
        // 조회하면 조회수 증가하도록
        bookClubService.incrementViewCount(bookClubId);
        BookClubResponse bookClubResponse = bookClubService.getBookClub(bookClubId);
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubResponse));
    }

    //ISBN13 으로 북클럽 목록 조회
    @GetMapping("/isbn")
    @Operation(summary = "책으로 북클럽 조회", description = "ISBN13으로 북클럽 목록을 조회합니다")
    public ResponseEntity<?> getBookClubByIsbn13(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "5") int size,
                                                 @RequestParam("bookId") final long bookId) {
        Pageable pageable = PageRequest.of(page-1, size);

        Page<BookClubListResponse> bookClubResponse = bookClubService.getBookClubByIsbn13(bookId,pageable);
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubResponse));
    }

    //유저가 가입한 북클럽 조회
    @GetMapping("/my")
    @Operation(summary = "유저가 가입한 북클럽 조회", description = "유저가 가입한 북클럽 목록을 조회합니다")
    public ResponseEntity<?> getUserBookClub(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "5") int size,
                                             @AuthenticationPrincipal CustomUserDetails userDetails
                                                ) {
        Pageable pageable = PageRequest.of(page-1, size);

        Page<BookClubListResponse> bookClubResponse = bookClubService.getBookClubByUserId(userDetails.getUser().getUserId(),pageable);
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubResponse));
    }

    //인기있는 북클럽 조회
    @GetMapping("/hotClub")
    @Operation(summary = "인기있는 북클럽 목록 조회", description = "멤버수, Completion, 생성일 순으로 상위 3개의 북클럽 목록 조회 API")
    public ResponseEntity<?> getHotBookClubs() {
        // BookClubService에서 상위 3개의 인기 북클럽을 가져옴
        List<BookClubListResponse> hotBookClubs = bookClubService.getHotBookClubs();

        return ResponseEntity.ok(BasicResponse.ofSuccess(hotBookClubs));
    }



}
