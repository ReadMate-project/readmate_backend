package com.readmate.ReadMate.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PageInfo {
    private int page;
    private int size; //한 페이지 당 15개씩 목록 조회하도록
    private int totalElements;
    private int totalPages;
}
