package com.readmate.ReadMate.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MVPResponse {
    private Long boardId;
    private String bookId;
    private String title;
    private String content;
    private Long userId;
    private String userNickname;
    private String profileImgUrl;
    private int likeCount;
    private int commentCount;
}
