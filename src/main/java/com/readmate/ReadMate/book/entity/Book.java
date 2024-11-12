package com.readmate.ReadMate.book.entity;

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
    @Column(name = "isbn13")
    private Long isbn13;  // ISBN13을 기본 키로 설정

    @Column(name = "book_title")
    private String title;

    @Column(name = "book_author")
    private String author;

    @Column(name = "total_pages")
    private Long totalPages;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "book_cover")
    private String bookCover;

    @Column(name = "genre")
    private String genre;  // 장르 필드 추가

    @Column(name = "del_yn", columnDefinition = "VARCHAR(1) default 'N'")
    @Builder.Default
    private String delYn = "N";

}
