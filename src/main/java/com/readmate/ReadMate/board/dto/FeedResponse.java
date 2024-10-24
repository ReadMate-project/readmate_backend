package com.readmate.ReadMate.board.dto;

import com.readmate.ReadMate.book.dto.res.BookResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FeedResponse {
    private String title;
    private String content;
    private String writeDate;
    private Long userId;
    private String profileImageUrl;
    private String nickname;
    private BookResponse bookResponse;
}
