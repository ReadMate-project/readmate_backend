package com.readmate.ReadMate.bookclub.bookClubChallenge.controller;

import com.readmate.ReadMate.bookclub.bookClubChallenge.service.BookClubChallengeService;
import com.readmate.ReadMate.bookclub.dailyMission.dto.ChallengeResponse;
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
import java.util.Collections;

@RestController
@RequiredArgsConstructor
    @RequestMapping("/api/v1/bookClub/challenges")
@Tag(name = "BookClubChallenge", description = "BookClubChallenge API")
public class BookClubChallengeController {

    private final BookClubChallengeService bookClubChallengeService;
    private final BookClubMissionService bookClubMissionService;

    /**
     * 오늘의 진행 상황
     */
    @GetMapping("/{bookClubId}")
    public ResponseEntity<?> getTodayChallenge(@AuthenticationPrincipal @Valid CustomUserDetails userDetails, @PathVariable final long bookClubId) {
        // 유저와 책 클럽 ID를 기반으로 오늘의 미션을 가져옵니다.
        ChallengeResponse challengeResponse = bookClubChallengeService.getClubChallenge(userDetails.getUser().getUserId(), bookClubId);

        // 미션이 비어 있으면 빈 리스트를 반환합니다.
        if (challengeResponse == null || challengeResponse.getMissionId() == null) {
            return ResponseEntity.ok(BasicResponse.ofCreateSuccess(Collections.emptyList()));  // 빈 리스트 반환
        }

        // 미션이 있으면 정상적으로 데이터를 반환합니다.
        return ResponseEntity.ok(BasicResponse.ofCreateSuccess(challengeResponse));// 미션이 있는 경우
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
    @GetMapping("/{bookClubId}/completed") // 적절한 엔드포인트 설정
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
