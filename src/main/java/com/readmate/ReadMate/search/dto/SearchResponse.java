package com.readmate.ReadMate.search.dto;

import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.book.entity.Book;
import com.readmate.ReadMate.bookclub.entity.BookClub;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SearchResponse {

    private List<Board> boards;
    private List<BookClub> bookClubs;
    private List<Book> books;
}
