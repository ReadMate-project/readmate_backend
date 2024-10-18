package com.readmate.ReadMate.bookclub.controller;

import com.readmate.ReadMate.bookclub.dto.req.BookClubRequest;

import com.readmate.ReadMate.bookclub.dto.res.BookClubListResponse;
import com.readmate.ReadMate.bookclub.dto.res.BookClubResponse;
import com.readmate.ReadMate.bookclub.entity.BookClub;
import com.readmate.ReadMate.bookclub.service.BookClubService;
import com.readmate.ReadMate.common.message.BasicResponse;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> createClub(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody BookClubRequest clubRequest) {
        return ResponseEntity.ok(BasicResponse.ofCreateSuccess(bookClubService.createClub(userDetails, clubRequest)));
    }

    @PatchMapping("/{bookClubId}")
    @Operation(summary = "북클럽 수정하기", description = "북클럽을 수정합니다")
    public ResponseEntity<?> updateClub(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long bookClubId, @RequestBody BookClubRequest clubRequest) {
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubService.updateClub(userDetails, bookClubId, clubRequest)));
    }

    @DeleteMapping("/{bookClubId}")
    @Operation(summary = "북클럽 삭제하기", description = "북클럽을 삭제합니다")
    public ResponseEntity<?> deleteClub(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long bookClubId) {
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

//        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubService.getBookClub(bookClubId)));

    }

    //북클럽이름 중복 확인
    @GetMapping("/checkClubName")
    @Operation(summary = "북클럽 이름 중복 체크", description = "북클럽 이름 사용 가능여부 확인합니다")
    public ResponseEntity<?> checkClubName(@RequestParam String clubName) {
        boolean isTaken = bookClubService.isBookClubNameTaken(clubName);
        return ResponseEntity.ok(isTaken);
    }

    //북클럽 조회수로 정렬
    @GetMapping("/popular")
    @Operation(summary = "인기있는 북클럽 정렬", description = "조회수가 높은 북클럽으로 정렬합니다")
    public ResponseEntity<List<BookClubListResponse>> getPopularBookClubs() {
        // 조회수로 정렬된 북클럽 리스트 가져오기
        List<BookClub> popularBookClubs = bookClubService.getBookClubsOrderedByViewCount();

        // BookClub을 BookClubListResponse로 변환
        List<BookClubListResponse> responseList = popularBookClubs.stream()
                .map(bookClub -> BookClubListResponse.builder()
                        .bookClubID(bookClub.getBookClubId())
                        .bookClubName(bookClub.getBookClubName())
                        .description(bookClub.getDescription())
                        .startDate(bookClub.getStartDate())
                        .endDate(bookClub.getEndDate())
                        .recruitmentStartDate(bookClub.getRecruitmentStartDate())
                        .recruitmentEndDate(bookClub.getRecruitmentEndDate())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }
}
