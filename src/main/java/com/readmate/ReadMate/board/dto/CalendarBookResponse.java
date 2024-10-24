package com.readmate.ReadMate.board.dto;

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
        private String isbnId;
        private String bookCoverUrl;

        public BookInfo(String isbnId, String bookCoverUrl) {
            this.isbnId = isbnId;
            this.bookCoverUrl = bookCoverUrl;
        }


    }


}
