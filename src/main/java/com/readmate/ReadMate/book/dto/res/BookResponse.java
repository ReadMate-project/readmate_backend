package com.readmate.ReadMate.book.dto.res;

import com.readmate.ReadMate.common.genre.Genre;
import lombok.*;
import org.w3c.dom.Text;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class BookResponse {

    private String isbn13;

    private String title;

    private String author;

    private Long totalPages;

    private String genre;
    private String description;

    private String publisher;

    private String bookCover;

}
