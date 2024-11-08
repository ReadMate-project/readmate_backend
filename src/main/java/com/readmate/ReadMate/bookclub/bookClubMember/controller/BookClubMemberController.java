package com.readmate.ReadMate.bookclub.bookClubMember.controller;


import com.readmate.ReadMate.board.dto.MVPResponse;
import com.readmate.ReadMate.board.service.BoardService;
import com.readmate.ReadMate.bookclub.bookClubMember.dto.BookClubJoinRequest;
import com.readmate.ReadMate.bookclub.bookClubMember.service.BookClubMemberService;
import com.readmate.ReadMate.common.message.BasicResponse;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookClubJoin/{bookClubId}")
@Tag(name = "BookClubMember", description = "BookClubMember API")
public class BookClubMemberController {

    @Autowired
    private BookClubMemberService bookClubMemberService;
    @Autowired
    private  BoardService boardService;

    @PostMapping()
    @Operation(summary = "북클럽 가입하기", description = "북클럽에 가입합니다")
    public ResponseEntity<?> joinClub(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @PathVariable final long bookClubId,
                                      @RequestBody @Valid BookClubJoinRequest bookClubJoinRequest){
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubMemberService.joinClub(bookClubId,bookClubJoinRequest,userDetails.getUser().getUserId())));

    }


    @DeleteMapping()
    @Operation(summary = "북클럽 탈퇴하기", description = "북클럽을 탈퇴합니다")
    public ResponseEntity<?> leaveClub(@AuthenticationPrincipal final CustomUserDetails userDetails,
                                       @PathVariable @NotNull final long bookClubId){
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubMemberService.leaveClub(bookClubId,userDetails.getUser().getUserId())));
    }

    @PostMapping("/approve/{userId}")
    @Operation(summary = "북클럽 가입 승인", description = "북클럽 멤버의 가입을 승인합니다")
    public ResponseEntity<?> approveMember(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @PathVariable Long bookClubId,
                                           @PathVariable Long userId) {
        // 가입 승인 로직 호출
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubMemberService.approveMember(bookClubId, userId)));
    }

    /**
     * 북클럽 가입 승인된 멤버 조회 메서드
     * @param userDetails 인증된 사용자 정보
     * @param bookClubId 북클럽 ID
     * @return 200
     */
    @GetMapping()
    @Operation(summary = "북클럽 멤버 조회", description = "북클럽 가입된 멤버를 조회합니다.")
    public ResponseEntity<?> findMember(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @PathVariable final long bookClubId) {
        // 가입 승인 로직 호출
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubMemberService.findMembers(bookClubId, userDetails.getUser().getUserId(),false)));
    }


    /**
     * 북클럽 가입 신청한 모든 멤버 조회
     */
    @GetMapping("/all")
    @Operation(summary = "북클럽 멤버 조회", description = "북클럽 가입된 모든 멤버를 조회합니다.")
    public ResponseEntity<?> findAllMember(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @PathVariable Long bookClubId) {
        // 가입 승인 로직 호출
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubMemberService.findMembers(bookClubId,userDetails.getUser().getUserId(),true)));
    }

    @GetMapping("/mvp")
    public ResponseEntity<BasicResponse<List<MVPResponse>>> getMVP(
            @RequestParam Long bookClubId) {

        List<MVPResponse> mvpResponses = boardService.getMVPResponse(bookClubId);

        return ResponseEntity.ok(BasicResponse.ofSuccess(mvpResponses));
    }

}
