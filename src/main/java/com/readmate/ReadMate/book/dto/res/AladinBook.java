package com.readmate.ReadMate.book.dto.res;

import lombok.Data;
import org.w3c.dom.Text;


@Data
public class AladinBook {
    private String title;
    private String author;
    private String description;
    private String isbn;
    private Long isbn13;
    private String publisher;
    private Long itemPage;
    private String cover;
    private String categoryName;
    private SubInfo subInfo;


}
