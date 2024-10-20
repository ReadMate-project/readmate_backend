package com.readmate.ReadMate.bookclub.controller;

import com.readmate.ReadMate.bookclub.entity.DailyMission;
import com.readmate.ReadMate.bookclub.service.BookClubChallengeService;
import com.readmate.ReadMate.common.message.BasicResponse;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookClub/challenge")
@Tag(name = "BookClubChallenge", description = "BookClubChallenge API")
public class BookClubChallengeController {

    private final BookClubChallengeService bookClubChallengeService;

    /**
     * 오늘의 진행 상황
     */
    @GetMapping
    public ResponseEntity<?> getTodayChallenge(@AuthenticationPrincipal CustomUserDetails userDetails,Long bookClubId){
        return ResponseEntity.ok(BasicResponse.ofCreateSuccess(bookClubChallengeService.getClubChallenge(userDetails,bookClubId
        )));

    }

    /**
     * 유저가 참여중인 챌린지 조회
     *
     */
    @GetMapping("/user")
    public ResponseEntity<?> getUserChallenge(@AuthenticationPrincipal CustomUserDetails userDetails){
        return ResponseEntity.ok(BasicResponse.ofCreateSuccess(bookClubChallengeService.getUserChallenge(userDetails
        )));

    }

    /**
     *미션을 완수한 User 조회하는 메서드
     */
    @GetMapping("/user/completed") // 적절한 엔드포인트 설정
    public ResponseEntity<?> getUserChallenge(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long dailyMissionId,
            @RequestParam LocalDate date) {

        return ResponseEntity.ok(BasicResponse.ofCreateSuccess(
                bookClubChallengeService.getCompletedMembers(dailyMissionId, date)
        ));
    }

}
