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

}
