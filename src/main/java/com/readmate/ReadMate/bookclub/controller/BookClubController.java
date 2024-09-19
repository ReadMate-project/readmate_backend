package com.readmate.ReadMate.bookclub.controller;

import com.readmate.ReadMate.bookclub.dto.req.BookClubRequest;

import com.readmate.ReadMate.bookclub.service.BookClubService;
import com.readmate.ReadMate.common.message.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookClub")
@Tag(name = "BookClub", description = "BookClub API")
public class BookClubController {


    private final BookClubService bookClubService;
    @PostMapping
    @Operation(summary = "북클럽 생성하기", description = "새로운 북클럽을 생성합니다")
    public  ResponseEntity<?> createClub(@RequestBody BookClubRequest clubRequest){
        return ResponseEntity.ok(BasicResponse.ofCreateSuccess(bookClubService.createClub(clubRequest)));
    }

    @PatchMapping("/{bookClubId}")
    @Operation(summary = "북클럽 수정하기", description = "북클럽을 수정합니다")
    public  ResponseEntity<?> updateClub(@PathVariable Long bookClubId,@RequestBody BookClubRequest clubRequest){
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubService.updateClub(bookClubId,clubRequest)));
    }

    @DeleteMapping("/{bookClubId}")
    @Operation(summary = "북클럽 삭제하기", description = "북클럽을 삭제합니다")
    public ResponseEntity<?> deleteClub(@PathVariable Long bookClubId){
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubService.deleteClub(bookClubId)));
    }

    @GetMapping()
    @Operation(summary = "북클럽 리스트 조회", description = "북클럽 리스트 조회합니다")
    public ResponseEntity<?> getClubList(){
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubService.getClubList()));
    }

    @GetMapping("/{bookClubId}")
    @Operation(summary = "북클럽 조회", description = "북클럽 조회합니다")
    public ResponseEntity<?> getBookClub(@PathVariable Long bookClubId){
        return ResponseEntity.ok(BasicResponse.ofSuccess(bookClubService.getBookClub(bookClubId)));

    }

    //북클럽이름 중복 확인
    @GetMapping("/checkClubName")
    @Operation(summary = "북클럽 이름 중복 체크", description = "북클럽 사용 가능여부 확인합니다")
    public ResponseEntity<?> checkClubName(@RequestParam String clubName) {
        boolean isTaken = bookClubService.isBookClubNameTaken(clubName);
        return ResponseEntity.ok(isTaken);
    }


}
