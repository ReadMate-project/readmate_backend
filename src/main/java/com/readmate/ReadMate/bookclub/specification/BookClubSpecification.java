package com.readmate.ReadMate.bookclub.specification;

import com.readmate.ReadMate.bookclub.entity.BookClub;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class BookClubSpecification {

    // 카테고리나 이름으로 검색할 수 있는 메서드
    public static Specification<BookClub> containsCategoryOrName(String keyword) {
        return (root, query, criteriaBuilder) -> {
//            Predicate categoryContains = criteriaBuilder.like(root.get("category"), "%" + keyword + "%");

            Predicate nameContains = criteriaBuilder.like(root.get("bookClubName"), "%" + keyword + "%");
            Predicate descriptionContains = criteriaBuilder.like(root.get("description"), "%" + keyword + "%");

//            return criteriaBuilder.or(categoryContains, nameContains);
            return criteriaBuilder.or(nameContains, descriptionContains);
        };
    }

    // 북클럽 ID 목록으로 필터링
    public static Specification<BookClub> bookClubIdIn(List<Long> bookClubIds) {
        return (root, query, criteriaBuilder) -> {
            if (bookClubIds == null || bookClubIds.isEmpty()) {
                return criteriaBuilder.conjunction(); // 빈 목록일 경우 모든 결과를 포함
            }
            return root.get("bookClubId").in(bookClubIds);
        };
    }
}
