package com.readmate.ReadMate.book.controller;

import com.readmate.ReadMate.book.dto.res.MyBookResponse;
import com.readmate.ReadMate.common.message.BasicResponse;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import com.readmate.ReadMate.book.service.MyBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/book")
public class MyBookController {
    private final MyBookService bookService;

    /**
     * 내가 추가한 책 조회하는 메서드
     */
    @GetMapping
    public ResponseEntity<BasicResponse<List<MyBookResponse>>> getMyBooks(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUser() == null) {
            BasicResponse<List<MyBookResponse>> errorResponse = BasicResponse.ofError("로그인을 진행해주세요", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        List<MyBookResponse> myBooks = bookService.getMyBooks(userDetails);
        BasicResponse<List<MyBookResponse>> response = BasicResponse.ofSuccess(myBooks);
        return ResponseEntity.ok(response);
    }

    /**
     * 내 서재에 책 추가하기
     */
    @PostMapping
    public ResponseEntity<BasicResponse<String>> addBookToMyLibrary(@RequestParam String isbn13, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUser() == null) {
            BasicResponse<String> errorResponse = BasicResponse.ofError("로그인을 진행해주세요", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        bookService.addBookToMyLibrary(isbn13, userDetails);
        BasicResponse<String> response = BasicResponse.ofSuccess("책이 서재에 추가되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 내 서재에 책 제거하기
     */
    @DeleteMapping
    public ResponseEntity<BasicResponse<String>> removeBookFromMyLibrary(@RequestParam String isbn13, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUser() == null) {
            BasicResponse<String> errorResponse = BasicResponse.ofError("로그인을 진행해주세요", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        bookService.removeBookFromMyLibrary(isbn13, userDetails);
        BasicResponse<String> response = BasicResponse.ofSuccess("책이 서재에서 제거되었습니다.");
        return ResponseEntity.ok(response);
    }

}
