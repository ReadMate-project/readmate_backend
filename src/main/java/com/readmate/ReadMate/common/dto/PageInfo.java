package com.readmate.ReadMate.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageInfo {
    private int pageNumber;
    private int pageSize;
    private int totalElements;
    private int totalPages;
}