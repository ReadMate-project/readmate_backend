package com.readmate.ReadMate.bookclub.dto.res;

import com.readmate.ReadMate.book.entity.Book;
import com.readmate.ReadMate.bookclub.entity.BookClubChallenge;
import lombok.*;


import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class BookClubChallengeResponse {
    private LocalDate readingStartDate;
    private LocalDate readingEndDate;
    private String bookCover;
    private String bookTitle;


    public void challengeResponse(BookClubChallenge challenge, Book book){
        this.readingStartDate = challenge.getStartDate();
        this.readingEndDate = challenge.getEndDate();
        this.bookCover = book.getBookCover();
        this.bookTitle = book.getTitle();

    }
}
