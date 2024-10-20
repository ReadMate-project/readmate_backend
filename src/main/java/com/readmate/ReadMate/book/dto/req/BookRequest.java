package com.readmate.ReadMate.book.dto.req;

import com.readmate.ReadMate.common.genre.Genre;
import lombok.*;
import org.w3c.dom.Text;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequest {

    private String title;
    private Long totalPages;
    private String isbn13;
    private String bookCover;
    //아래 제거 여부 나중에 논의 예정
    private String author;
     private String genre;
    private String description;
    private String publisher;

}
