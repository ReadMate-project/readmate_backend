package com.readmate.ReadMate.search.service;

import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.board.entity.BoardType;
import com.readmate.ReadMate.board.repository.BoardRepository;
import com.readmate.ReadMate.board.specification.BoardSpecification;
import com.readmate.ReadMate.book.entity.Book;
import com.readmate.ReadMate.book.repository.BookRepository;
import com.readmate.ReadMate.book.specification.BookSpecification;
import com.readmate.ReadMate.bookclub.bookClubMember.service.BookClubMemberService;
import com.readmate.ReadMate.bookclub.club.entity.BookClub;
import com.readmate.ReadMate.bookclub.bookClubMember.repository.BookClubMemberRepository;
import com.readmate.ReadMate.bookclub.club.repository.BookClubRepository;
import com.readmate.ReadMate.bookclub.club.specification.BookClubSpecification;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import com.readmate.ReadMate.search.dto.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService { //북클럽과 게시판 글 통합해서 검색되게 구현

    private final BoardRepository boardRepository;
    private final BookClubRepository bookClubRepository;
    private final BookClubMemberService bookClubMemberService;
    private final BookRepository bookRepository;

    public SearchResponse searchAll(String keyword, UserDetails userDetails) {
        List<Board> boards;
        List<BookClub> bookClubs;
        List<Book> books;

        // 1. 공지사항은 제외하고 검색
        boards = boardRepository.findAll(Specification.where(BoardSpecification.containsTitleOrContent(keyword, new ArrayList<>())));

        if (userDetails instanceof CustomUserDetails) {
            Long userId = ((CustomUserDetails) userDetails).getUser().getUserId();

            // 사용자가 소속된 북클럽 ID 리스트 가져오기
            List<Long> userBookClubIds = bookClubMemberService.findBookClubIdsByUserId(userId);

            // BOARD와 FEED 검색
            boards = boards.stream()
                    .filter(board -> board.getBoardType() != BoardType.NOTICE) // 공지사항 제외
                    .collect(Collectors.toList());

            // CLUB_BOARD 게시글 추가
            if (!userBookClubIds.isEmpty()) {
                List<Board> clubBoards = boardRepository.findAll(Specification.where(BoardSpecification.containsTitleOrContent(keyword, userBookClubIds))
                        .and(BoardSpecification.clubBoardIn(userBookClubIds))); // 북클럽 게시판도 포함
                boards.addAll(clubBoards);
            }

            bookClubs = bookClubRepository.findAll(Specification.where(BookClubSpecification.containsCategoryOrName(keyword))
                    .and(BookClubSpecification.bookClubIdIn(userBookClubIds)));
        } else {
            bookClubs = bookClubRepository.findAll(Specification.where(BookClubSpecification.containsCategoryOrName(keyword)));
        }

        books = bookRepository.findAll(BookSpecification.containsTitleOrAuthorOrDescriptionOrCategory(keyword));

        // 최신순 정렬
        boards.sort(Comparator.comparing(Board::getCreatedAt).reversed());
        bookClubs.sort(Comparator.comparing(BookClub::getCreatedAt).reversed());
        books.sort(Comparator.comparing(Book::getCreatedAt).reversed());

        return new SearchResponse(boards, bookClubs, books);
    }

}
