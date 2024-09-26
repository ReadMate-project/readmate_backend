package com.readmate.ReadMate.book.dto.res;

import lombok.Data;


@Data
public class AladinBook {
    private String title;
    private String author;
    private String description;
    private String isbn;
    private String isbn13;
    private String publisher;
    private Long itemPage;
    private String cover;
    private String categoryName;
    private SubInfo subInfo;


}
