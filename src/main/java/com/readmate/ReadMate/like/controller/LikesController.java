package com.readmate.ReadMate.like.controller;

import com.readmate.ReadMate.like.entity.Likes;
import com.readmate.ReadMate.like.service.LikesSerivce;
import com.readmate.ReadMate.login.dto.res.BasicResponse;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Tag(name = "like", description = "Like API")
@RequestMapping("/api/v1/like")
public class LikesController {

    private final LikesSerivce likesService;

    @PostMapping("/{boardId}")
    @Operation(summary = "게시물 좋아요", description = "게시물 좋아요 API")
    public ResponseEntity<BasicResponse<Likes>> toggleLike(@PathVariable("boardId") Long boardId,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        Likes updatedLike = likesService.toggleLike(boardId, userDetails);
        BasicResponse<Likes> response = BasicResponse.ofSuccess(updatedLike);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/count/{boardId}")
    @Operation(summary = "좋아요 개수 조회", description = "특정 게시물의 좋아요 개수를 반환하는 API")
    public ResponseEntity<BasicResponse<Integer>> getLikeCount(@PathVariable("boardId") Long boardId) {
        int likeCount = likesService.countLikesByBoardId(boardId);
        BasicResponse<Integer> response = BasicResponse.ofSuccess(likeCount);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //한 게시글에 대한 좋아요 목록조회
    @GetMapping("/list/{boardId}")
    @Operation(summary = "게시글 좋아요 목록 조회", description = "특정 게시글에 좋아요를 누른 사용자 목록을 반환하는 API")
    public ResponseEntity<BasicResponse<List<Likes>>> getLikesByBoardId(@PathVariable("boardId") Long boardId) {
        List<Likes> likesList = likesService.getLikesByBoardId(boardId);
        BasicResponse<List<Likes>> response = BasicResponse.ofSuccess(likesList);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    //전체 좋아요 목록조회
    @GetMapping("/list")
    @Operation(summary = "전체 좋아요 목록 조회", description = "모든 좋아요 데이터를 반환하는 API")
    public ResponseEntity<BasicResponse<List<Likes>>> getAllLikes() {
        List<Likes> allLikes = likesService.getAllLikes();
        BasicResponse<List<Likes>> response = BasicResponse.ofSuccess(allLikes);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



}
