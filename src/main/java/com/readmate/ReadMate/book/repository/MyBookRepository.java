package com.readmate.ReadMate.book.repository;

import com.readmate.ReadMate.book.entity.MyBook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MyBookRepository extends JpaRepository<MyBook,Long> {

    // userId와 delYn 조건을 사용하여 MyBook 엔티티 조회
    List<MyBook> findByUserIdAndDelYnFalse(Long userId);

    // userId와 isbn13을 사용하여 특정 책을 조회, 삭제하지 않은 상태
    MyBook findByUserIdAndIsbn13AndDelYnFalse(Long userId, String isbn13);

    // userId와 isbn13을 사용하여 특정 책을 조회
    MyBook findByUserIdAndIsbn13(Long userId, String isbn13);


}
