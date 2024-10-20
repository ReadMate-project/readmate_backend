package com.readmate.ReadMate.book.dto.res;

import com.readmate.ReadMate.book.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyBookResponse {
    private Long myBookId;
    private Book book;
    private LocalDate lastReadDate;
}
