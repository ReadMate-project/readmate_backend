package com.readmate.ReadMate.board.dto.response;

import com.readmate.ReadMate.board.entity.BoardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class BoardResponse {

    private Long boardId;
    private BoardType boardType;
    private Long bookId;
    private Long bookclubId;
    private String content;
    private String createdAt;
    private String title;
    private Long userId;
    private List<String> imageUrls;
    private int commentCount;
    private int likeCount;
    private String nickname;
    private String profileImageUrl;
}
