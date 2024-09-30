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
    private Long totalPages;
    private Long isbn13;
    private String bookCover;
    //아래 제거 여부 나중에 논의 예정
    private String author;
    // private Genre genre;
    private String description;
    private String publisher;

}
