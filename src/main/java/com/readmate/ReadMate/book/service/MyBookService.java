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

        List<MyBook> myBookList = myBookRepository.findByUserIdAndDelYnFalse(userId);
        List<MyBookResponse> myBookResponseList = new ArrayList<>();

        for (MyBook myBook : myBookList){
            Book book = bookRepository.findByIsbn13(myBook.getIsbn13())
                    .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));
            myBookResponseList.add(new MyBookResponse(myBook.getMyBookId(), book, myBook.getLastReadDate()));
        }

        return myBookResponseList;
    }



    public void saveMyBook(CustomUserDetails user, Book saveBook) {
        // 이미 서재에 있는지 확인
        MyBook existingMyBook = myBookRepository.findByUserIdAndIsbn13AndDelYnFalse(user.getUser().getUserId(), saveBook.getIsbn13());
        if (existingMyBook == null) {

            MyBook newBook = MyBook.builder()
                    .isbn13(saveBook.getIsbn13())
                    .userId(user.getUser().getUserId())
                    .lastReadDate(LocalDate.now())
                    .build();
            myBookRepository.save(newBook);
        }
    }

    public void addBookToMyLibrary(Long isbn13, CustomUserDetails userDetails) {
        Book book = bookService.saveBookByIsbn(isbn13);

        if (book == null) {
            throw new CustomException(ErrorCode.BOOK_NOT_FOUND);
        }

        MyBook myBook = myBookRepository.findByUserIdAndIsbn13(userDetails.getUser().getUserId(), book.getIsbn13());

        if (myBook == null) {
            myBook = MyBook.builder()
                    .isbn13(book.getIsbn13())
                    .userId(userDetails.getUser().getUserId())
                    .lastReadDate(LocalDate.now())
                    .delYn(false)
                    .build();
            myBookRepository.save(myBook);
        } else if (!myBook.isDelYn()) {
            myBook.setDelYn(false);
            myBook.setLastReadDate(LocalDate.now());
            myBookRepository.save(myBook);
        } else {
            throw new CustomException(ErrorCode.BOOK_ALREADY_IN_LIBRARY);
        }
    }


    public void removeBookFromMyLibrary(Long isbn13, CustomUserDetails userDetails) {
        Book book = bookRepository.findByIsbn13(isbn13)
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

        MyBook myBook = myBookRepository.findByUserIdAndIsbn13AndDelYnFalse(userDetails.getUser().getUserId(), book.getIsbn13());

        if (myBook != null) {
            myBook.setDelYn(true);
            myBookRepository.save(myBook);
        } else {
            throw new CustomException(ErrorCode.BOOK_NOT_IN_LIBRARY);
        }
    }
}
