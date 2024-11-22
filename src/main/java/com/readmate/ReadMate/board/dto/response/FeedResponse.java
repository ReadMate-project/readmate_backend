package com.readmate.ReadMate.board.dto.response;

import com.readmate.ReadMate.book.dto.res.BookResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FeedResponse {
    private Long boardId;
    private String title;
    private String content;
    private String writeDate;
    private Long userId;
    private String profileImageUrl;
    private String nickname;
    private BookResponse bookResponse;
    private int likeCount;
    private int commentCount;
}
