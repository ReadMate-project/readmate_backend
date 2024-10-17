package com.readmate.ReadMate.book.repository;

import com.readmate.ReadMate.book.entity.MyBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyBookRepository extends JpaRepository<MyBook,Long> {

}
