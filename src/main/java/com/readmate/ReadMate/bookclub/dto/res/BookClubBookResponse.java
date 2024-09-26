package com.readmate.ReadMate.bookclub.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookClubBookResponse {
    private Long bookClubBookId;
    private Long isbn;
    private boolean isActive;
    private Long totalPage;
}
