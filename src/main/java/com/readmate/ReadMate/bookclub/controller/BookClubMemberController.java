package com.readmate.ReadMate.bookclub.controller;


import com.readmate.ReadMate.bookclub.dto.req.BookClubJoinRequest;
import com.readmate.ReadMate.bookclub.service.BookClubMemberService;
import com.readmate.ReadMate.common.message.BasicResponse;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookClubJoin")
@Tag(name = "BookClubMember", description = "BookClubMember API")
public class BookClubMemberController {

    @Autowired
    private BookClubMemberService bookClubMemberService;

    /**
     * 북클럽 가입 신청 메소드
     * @param userDetails 인증된 사용자 정보
     * @param bookClubId 북클럽 ID
     * @param bookClubJoinRequest 가입 신청 정보
     * @return 가입 신청 결과
     */
    @PostMapping("/{bookClubId}")
    @Operation(summary = "북클럽 가입하기", description = "북클럽에 가입합니다")
    public ResponseEntity<?> joinClub(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @PathVariable Long bookClubId,
                                      @RequestBody BookClubJoinRequest bookClubJoinRequest){
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubMemberService.joinClub(bookClubId,bookClubJoinRequest,userDetails)));

    }

    /**
     * 북클럽 탈퇴 메서드
     * @param userDetails 인증된 사용자 정보
     * @param bookClubId 북클럽 ID
     * @return 가입 신청 결과
     */
    @DeleteMapping("/{bookClubId}")
    @Operation(summary = "북클럽 탈퇴하기", description = "북클럽을 탈퇴합니다")
    public ResponseEntity<?> leaveClub(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @PathVariable Long bookClubId){
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubMemberService.leaveClub(bookClubId,userDetails)));
    }
    /**
     * 북클럽 가입 승인 메서드
     * @param userDetails 인증된 사용자 정보
     * @param bookClubId 북클럽 ID
     * @param userId 승인할 멤버 ID
     * @return 승인 결과
     */
    @PostMapping("/{bookClubId}/approve/{userId}")
    @Operation(summary = "북클럽 가입 승인", description = "북클럽 멤버의 가입을 승인합니다")
    public ResponseEntity<?> approveMember(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @PathVariable Long bookClubId,
                                           @PathVariable Long userId) {
        // 가입 승인 로직 호출
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubMemberService.approveMember(bookClubId, userId, userDetails)));
    }

    /**
     * 북클럽 가입 멤버 조회 메서드
     * @param userDetails 인증된 사용자 정보
     * @param bookClubId 북클럽 ID
     * @return 승인 결과
     */
    @GetMapping("/{bookClubId}")
    @Operation(summary = "북클럽 멤버 조회", description = "북클럽 멤버를 조회합니다.")
    public ResponseEntity<?> findMember(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @PathVariable Long bookClubId) {
        // 가입 승인 로직 호출
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubMemberService.findMember(bookClubId,userDetails)));
    }


}
