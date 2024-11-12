package com.readmate.ReadMate.bookclub.club.specification;

import com.readmate.ReadMate.bookclub.club.entity.BookClub;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
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

    // 삭제되지 않은 북클럽 필터
    public static Specification<BookClub> withDelYnFalse(){
        return ((root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("delYn")));
    }

    // 모집중 필터
    public static Specification<BookClub> isRecruiting(LocalDate today){
        System.out.println("모집중인 북클럽 조회 ");
        return(root ,query, criteriaBuilder)->criteriaBuilder.and(
                criteriaBuilder.lessThanOrEqualTo(root.get("recruitmentStartDate"),today),
                criteriaBuilder.greaterThanOrEqualTo(root.get("recruitmentEndDate"),today)
        );
    }
    // 진행중 필터
    public static Specification<BookClub> isInProgress(LocalDate today){
        System.out.println("진행중인 북클럽 조회 ");
        return(root ,query, criteriaBuilder)->criteriaBuilder.and(
                criteriaBuilder.lessThanOrEqualTo(root.get("startDate"),today),
                criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"),today)
        );
    }

}
