package com.readmate.ReadMate.book.service;

import com.readmate.ReadMate.book.dto.res.MyBookResponse;
import com.readmate.ReadMate.book.entity.Book;
import com.readmate.ReadMate.book.entity.MyBook;

import com.readmate.ReadMate.book.repository.BookRepository;
import com.readmate.ReadMate.common.exception.CustomException;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import com.readmate.ReadMate.book.repository.MyBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyBookService {

    private final BookRepository bookRepository;
    private final MyBookRepository myBookRepository;
    private final BookService bookService;

    public List<MyBookResponse> getMyBooks(CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getUserId();

        List<MyBook> myBookList = myBookRepository.findByUser_UserIdAndDelYn(userId, "N");
        List<MyBookResponse> myBookResponseList = new ArrayList<>();

        for (MyBook myBook : myBookList){
            Book book = bookRepository.findById(myBook.getBook().getIsbn13())
                    .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));
            myBookResponseList.add(new MyBookResponse(myBook.getMyBookId(), book, myBook.getLastReadDate()));
        }

        return myBookResponseList;
    }



    public void saveMyBook(CustomUserDetails user, Book saveBook) {
        // 이미 서재에 있는지 확인
        MyBook existingMyBook = myBookRepository.findByUserAndBookAndDelYn(user.getUser(), saveBook, "N");
        if (existingMyBook == null) {

            MyBook newBook = MyBook.builder()
                    .book(saveBook)
                    .user(user.getUser())
                    .lastReadDate(LocalDate.now())
                    .build();
            myBookRepository.save(newBook);
        }
    }

    public void addBookToMyLibrary(String isbn13, CustomUserDetails userDetails) {
        Book book = bookService.saveBookByIsbn(isbn13);

        if (book == null) {
            throw new CustomException(ErrorCode.BOOK_NOT_FOUND);
        }

        MyBook myBook = myBookRepository.findByUserAndBook(userDetails.getUser(), book);

        if (myBook == null) {
            myBook = MyBook.builder()
                    .book(book)
                    .user(userDetails.getUser())
                    .lastReadDate(LocalDate.now())
                    .delYn("N")
                    .build();
            myBookRepository.save(myBook);
        } else if (myBook.getDelYn().equals("Y")) {
            myBook.setDelYn("N");
            myBook.setLastReadDate(LocalDate.now());
            myBookRepository.save(myBook);
        } else {
            throw new CustomException(ErrorCode.BOOK_ALREADY_IN_LIBRARY);
        }
    }


    public void removeBookFromMyLibrary(String isbn13, CustomUserDetails userDetails) {
        Book book = bookRepository.findByIsbn13(isbn13)
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

        MyBook myBook = myBookRepository.findByUserAndBookAndDelYn(userDetails.getUser(), book, "N");

        if (myBook != null) {
            myBook.setDelYn("Y");
            myBookRepository.save(myBook);
        } else {
            throw new CustomException(ErrorCode.BOOK_NOT_IN_LIBRARY);
        }
    }
}
