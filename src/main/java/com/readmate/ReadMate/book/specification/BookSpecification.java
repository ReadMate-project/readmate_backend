package com.readmate.ReadMate.book.specification;

import com.readmate.ReadMate.book.entity.Book;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecification {

    // 제목, 저자, 설명, 카테고리로 검색할 수 있는 Specification
    public static Specification<Book> containsTitleOrAuthorOrDescriptionOrCategory(String keyword) {
        return (root, query, criteriaBuilder) -> {
            Predicate titleContains = criteriaBuilder.like(root.get("title"), "%" + keyword + "%");
            Predicate authorContains = criteriaBuilder.like(root.get("author"), "%" + keyword + "%");
            Predicate descriptionContains = criteriaBuilder.like(root.get("description"), "%" + keyword + "%");
//            Predicate categoryContains = criteriaBuilder.like(root.get("genre"), "%" + keyword + "%");

            return criteriaBuilder.or(titleContains, authorContains, descriptionContains);
//            return criteriaBuilder.or(titleContains, authorContains, descriptionContains, categoryContains);
        };
    }
}
