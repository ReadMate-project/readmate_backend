package com.readmate.ReadMate.bookclub.bookClubChallenge.controller;

import com.readmate.ReadMate.bookclub.bookClubChallenge.service.BookClubChallengeService;
import com.readmate.ReadMate.bookclub.dailyMission.service.BookClubMissionService;
import com.readmate.ReadMate.common.message.BasicResponse;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookClub/{bookClubId}/challenges")
@Tag(name = "BookClubChallenge", description = "BookClubChallenge API")
public class BookClubChallengeController {

    private final BookClubChallengeService bookClubChallengeService;
    private final BookClubMissionService bookClubMissionService;

    /**
     * 오늘의 진행 상황
     */
    @GetMapping
    public ResponseEntity<?> getTodayChallenge(@AuthenticationPrincipal @Valid CustomUserDetails userDetails, @PathVariable final long bookClubId){
        return ResponseEntity.ok(BasicResponse.ofCreateSuccess(bookClubChallengeService.getClubChallenge(userDetails.getUser().getUserId(),bookClubId
        )));
    }

    /**
     * 유저가 참여중인 챌린지 조회
     */
    @GetMapping("/my")
    public ResponseEntity<?> getUserChallenge( @AuthenticationPrincipal CustomUserDetails userDetails){
        return ResponseEntity.ok(BasicResponse.ofCreateSuccess(bookClubChallengeService.getUserChallenge(userDetails.getUser().getUserId()
        )));
    }

    /**e
     *미션을 완수한 User 조회하는 메서드
     */
    @GetMapping("/completed") // 적절한 엔드포인트 설정
    public ResponseEntity<?> getUserChallenge(
            @PathVariable Long bookClubId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long dailyMissionId,
            @RequestParam LocalDate date) {

        return ResponseEntity.ok(BasicResponse.ofCreateSuccess(
                bookClubMissionService.getCompletedMembers(bookClubId, userDetails.getUser().getUserId(), dailyMissionId, date)
        ));
    }

}
