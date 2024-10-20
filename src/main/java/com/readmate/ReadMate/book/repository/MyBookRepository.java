package com.readmate.ReadMate.book.repository;

import com.readmate.ReadMate.book.entity.Book;
import com.readmate.ReadMate.book.entity.MyBook;
import com.readmate.ReadMate.login.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MyBookRepository extends JpaRepository<MyBook,Long> {

//    List<MyBook> findByUserId(Long userId);
    List<MyBook> findByUser_UserId(Long userId);



    List<MyBook> findByUser_UserIdAndDelYn(Long userId, String n);

    MyBook findByUserAndBookAndDelYn(User user, Book saveBook, String n);

    MyBook findByUserAndBook(User user, Book saveBook);

}
