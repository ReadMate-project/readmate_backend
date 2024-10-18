package com.readmate.ReadMate.book.repository;

import com.readmate.ReadMate.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book,Long> , JpaSpecificationExecutor<Book> {
    @Query("SELECT b FROM Book b WHERE b.isbn13 = :isbn13")
    Optional<Book> findByIsbn13(@Param("isbn13") Long isbn13);
}
