package com.readmate.ReadMate.book.entity;

import com.readmate.ReadMate.common.genre.Genre;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long bookId;
    @Column(name = "book_title")
    private String title;
    @Column(name = "book_author")
    private String author;
    @Column(name = "total_pages")
    private Long totalPages;
//    @Column(name = "genre")
//    private Genre genre;
    @Column(name = "description")
    private String description;
    @Column(name = "publisher")
    private String publisher;
    @Column(name = "isbn13")
    private Long isbn13;
    @Column(name = "book_cover")
    private String bookCover;


    @Column(name = "del_yn", columnDefinition = "VARCHAR(1) default 'N'")
    @Builder.Default
    private String delYn = "N";
}
