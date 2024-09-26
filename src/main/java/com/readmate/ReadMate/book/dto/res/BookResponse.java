package com.readmate.ReadMate.book.dto.res;

import com.readmate.ReadMate.common.genre.Genre;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class BookResponse {

    private Long bookId;

    private String title;

    private String author;

    private Long totalPages;

    private Genre genre;
    private String description;

    private String publisher;

    private Long isbn13;

    private String bookCover;
}
