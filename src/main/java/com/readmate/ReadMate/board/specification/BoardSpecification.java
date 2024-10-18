package com.readmate.ReadMate.board.specification;

import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.board.entity.BoardType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class BoardSpecification {

    // 제목과 내용으로 검색할 수 있다.
    public static Specification<Board> containsTitleOrContent(String keyword, List<Long> userBookClubIds) {
        return (root, query, criteriaBuilder) -> {
            Predicate isNotNotice = criteriaBuilder.notEqual(root.get("boardType"), BoardType.NOTICE); // 공지사항은 검색에서 제외
            Predicate titleContains = criteriaBuilder.like(root.get("title"), "%" + keyword + "%");
            Predicate contentContains = criteriaBuilder.like(root.get("content"), "%" + keyword + "%");

            // 북클럽 게시판 글 접근 제한
            Predicate isClubBoard = criteriaBuilder.equal(root.get("boardType"), BoardType.CLUB_BOARD);
            Predicate clubBoardAccess = userBookClubIds.isEmpty() ?
                    criteriaBuilder.isFalse(isClubBoard) : // 사용자가 북클럽의 회원이 아닌 경우 CLUB_BOARD 제외
                    root.get("bookclubId").in(userBookClubIds); // 현재 사용자가 소속된 북클럽 ID 포함

            return criteriaBuilder.and(isNotNotice,
                    criteriaBuilder.or(titleContains, contentContains),
                    clubBoardAccess); // clubBoardAccess 추가
        };
    }

    // 북클럽 ID에 해당하는 게시글 찾기
    public static Specification<Board> clubBoardIn(List<Long> userBookClubIds) {
        return (root, query, criteriaBuilder) -> {
            return root.get("bookclubId").in(userBookClubIds);
        };
    }
}
