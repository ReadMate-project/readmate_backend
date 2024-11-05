package com.readmate.ReadMate.bookclub.club.controller;

import com.readmate.ReadMate.bookclub.club.dto.req.BookClubRequest;
import com.readmate.ReadMate.bookclub.club.dto.res.BookClubResponse;
import com.readmate.ReadMate.bookclub.club.service.BookClubService;
import com.readmate.ReadMate.common.message.BasicResponse;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookClub")
@Tag(name = "BookClub", description = "BookClub API")
public class BookClubController {


    private final BookClubService bookClubService;

    @PostMapping
    @Operation(summary = "북클럽 생성하기", description = "새로운 북클럽을 생성합니다")
    public ResponseEntity<?> createClub(@AuthenticationPrincipal final CustomUserDetails userDetails, @RequestBody BookClubRequest clubRequest) {
        return ResponseEntity.ok(BasicResponse.ofCreateSuccess(bookClubService.createClub(userDetails, clubRequest)));
    }

    @PatchMapping("/{bookClubId}")
    @Operation(summary = "북클럽 수정하기", description = "북클럽을 수정합니다")
    public ResponseEntity<?> updateClub(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable long bookClubId, @RequestBody BookClubRequest clubRequest) {
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubService.updateClub(userDetails, bookClubId, clubRequest)));
    }

    @DeleteMapping("/{bookClubId}")
    @Operation(summary = "북클럽 삭제하기", description = "북클럽을 삭제합니다")
    public ResponseEntity<?> deleteClub(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable long bookClubId) {
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubService.deleteClub(userDetails, bookClubId)));
    }

    @GetMapping()
    @Operation(summary = "북클럽 리스트 조회", description = "북클럽 리스트 조회합니다")
    public ResponseEntity<?> getClubList() {
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubService.getClubList()));
    }

    @GetMapping("/{bookClubId}")
    @Operation(summary = "북클럽 조회", description = "북클럽 조회합니다")
    public ResponseEntity<?> getBookClub(@PathVariable(name = "bookClubId") Long bookClubId) {
        // 조회하면 조회수 증가하도록
        bookClubService.incrementViewCount(bookClubId);
        BookClubResponse bookClubResponse = bookClubService.getBookClub(bookClubId);
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubResponse));

    }

}
