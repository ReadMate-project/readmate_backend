package com.readmate.ReadMate.book.controller;

import com.readmate.ReadMate.login.security.CustomUserDetails;
import com.readmate.ReadMate.book.service.MyBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/book")
public class MyBookController {
    private final MyBookService bookService;

    /**
     * 내가 추가한 책 조회하는 메서드
     */
    public ResponseEntity<?> getMyBooks(@AuthenticationPrincipal CustomUserDetails userDetails){
        return ResponseEntity.ok(bookService.getMyBooks(userDetails));
    }



}
