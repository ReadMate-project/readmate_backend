package com.readmate.ReadMate.board.dto;

import com.readmate.ReadMate.board.entity.BoardType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardRequest {

    private Long userId;
    private Long bookId;
    private Long bookclubId;
    private Integer totalPages;
    private Integer currentPage;
    private String content;
    private String title;

    //어떤 게시판인지르 알아야 각각에 대해서 처리가 가능 -> 프론트에서 받을 것이다.
    private BoardType boardType;

}
