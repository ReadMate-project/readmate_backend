package com.readmate.ReadMate.book.controller;

import com.readmate.ReadMate.book.dto.req.BookRequest;
import com.readmate.ReadMate.book.service.BookService;
import com.readmate.ReadMate.common.message.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/book")
@Tag(name = "Book", description = "Book API")
public class BookController {
    private final BookService bookService;

    @PostMapping
    @Operation(summary = "책 추가하기", description = "새로운 책을 추가합니다")
    public ResponseEntity<?> createBook(@RequestBody BookRequest bookRequest){
        return ResponseEntity.ok(BasicResponse.ofCreateSuccess(bookService.saveBook(bookRequest)));
    }

    @PostMapping("/{isbn13}")
    @Operation(summary = "ISBN13으로 책 추가하기", description = "ISBN13으로 새로운 책을 추가합니다")
    public ResponseEntity<?> createBookByIsbn(@PathVariable Long isbn13){
        return ResponseEntity.ok(BasicResponse.ofCreateSuccess(bookService.saveBookByIsbn(isbn13)));
    }

    // ID 기반
    @GetMapping("/{bookId}")
    @Operation(summary = "책ID 로 조회하기", description = "책 ID로 책을 조회합니다")
    public ResponseEntity<?> getBookById(@PathVariable Long bookId){
        return ResponseEntity.ok(BasicResponse.ofCreateSuccess(bookService.getBookById(bookId)));
    }

    @GetMapping("/isbn/{isbn13}")
    @Operation(summary = "책 ISBN13 으로 조회하기", description = "책 ISBN13 으로 책을 조회합니다")
    public ResponseEntity<?> getBookByIsbn(@PathVariable Long isbn13){
        return ResponseEntity.ok(BasicResponse.ofCreateSuccess(bookService.getBookByIsbn(isbn13)));
    }



}
