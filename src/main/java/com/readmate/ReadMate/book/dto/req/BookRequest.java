package com.readmate.ReadMate.book.dto.req;

import com.readmate.ReadMate.common.genre.Genre;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequest {

    private String title;
    private String author;
    private Long totalPages;
    private Genre genre;
    private String description;
    private String publisher;
    private Long isbn13;
    private String bookCover;
}
