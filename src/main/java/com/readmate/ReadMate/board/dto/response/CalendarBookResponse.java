package com.readmate.ReadMate.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
@Getter
@AllArgsConstructor
public class CalendarBookResponse {
    private String date;
    private List<BookInfo> books;

    @Getter
    public static class BookInfo {
        private Long isbnId;
        private String bookCoverUrl;

        public BookInfo(Long isbnId, String bookCoverUrl) {
            this.isbnId = isbnId;
            this.bookCoverUrl = bookCoverUrl;
        }


    }


}
