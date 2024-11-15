package com.readmate.ReadMate.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private Long commentId;
    private Long boardId;
    private Long userId;
    private String content;
    private String createdAt;
    private String nickname;
    private String profileImageUrl;
}
