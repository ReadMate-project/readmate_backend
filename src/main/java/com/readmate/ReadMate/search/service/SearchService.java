package com.readmate.ReadMate.search.service;

import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.board.entity.BoardType;
import com.readmate.ReadMate.board.repository.BoardRepository;
import com.readmate.ReadMate.board.specification.BoardSpecification;
import com.readmate.ReadMate.book.entity.Book;
import com.readmate.ReadMate.book.repository.BookRepository;
import com.readmate.ReadMate.book.specification.BookSpecification;
import com.readmate.ReadMate.bookclub.entity.BookClub;
import com.readmate.ReadMate.bookclub.repository.BookClubMemberRepository;
import com.readmate.ReadMate.bookclub.repository.BookClubRepository;
import com.readmate.ReadMate.bookclub.service.BookClubMemberService;
import com.readmate.ReadMate.bookclub.specification.BookClubSpecification;
import com.readmate.ReadMate.login.entity.User;
import com.readmate.ReadMate.login.repository.UserRepository;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import com.readmate.ReadMate.login.service.UserService;
import com.readmate.ReadMate.search.dto.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService { //북클럽과 게시판 글 통합해서 검색되게 구현

    private final BoardRepository boardRepository;
    private final BookClubRepository bookClubRepository;
    private final BookClubMemberRepository bookClubMemberRepository;
    private final BookRepository bookRepository;

    public SearchResponse searchAll(String keyword, UserDetails userDetails) {
        List<Board> boards;
        List<BookClub> bookClubs;
        List<Book> books;

        // 1. 공지사항은 제외하고 검색
        boards = boardRepository.findAll(Specification.where(BoardSpecification.containsTitleOrContent(keyword, new ArrayList<>())));
        System.out.println("Boards found: " + boards.size());

        if (userDetails instanceof CustomUserDetails) {
            Long userId = ((CustomUserDetails) userDetails).getUser().getUserId();
            System.out.println("Authenticated user ID: " + userId);

            // 사용자가 소속된 북클럽 ID 리스트 가져오기
            List<Long> userBookClubIds = bookClubMemberRepository.findBookClubIdsByUserId(userId);
            System.out.println("User's book clubs: " + userBookClubIds);

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
            System.out.println("Filtered boards: " + boards.size());

            bookClubs = bookClubRepository.findAll(Specification.where(BookClubSpecification.containsCategoryOrName(keyword))
                    .and(BookClubSpecification.bookClubIdIn(userBookClubIds)));
        } else {
            System.out.println("Unauthenticated user");
            bookClubs = bookClubRepository.findAll(Specification.where(BookClubSpecification.containsCategoryOrName(keyword)));
        }

        books = bookRepository.findAll(BookSpecification.containsTitleOrAuthorOrDescriptionOrCategory(keyword));
        System.out.println("Books found: " + books.size());

        return new SearchResponse(boards, bookClubs, books);
    }



//    public SearchResponse searchAll(String keyword, UserDetails userDetails) {
//        List<Board> boards;
//        List<BookClub> bookClubs;
//        List<Book> books;
//
//        // 1. 공지사항은 제외하고 검색
//        boards = boardRepository.findAll(Specification.where(BoardSpecification.containsTitleOrContent(keyword)));
//
//        // 2. 북클럽 게시글 검색 (로그인한 유저가 속한 북클럽만)
//        if (userDetails instanceof CustomUserDetails) {
//            Long userId = ((CustomUserDetails) userDetails).getUser().getUserId();
//
//            // 유저가 속한 북클럽들 조회
//            List<Long> userBookClubIds = bookClubMemberRepository.findBookClubIdsByUserId(userId);
//
//            // 유저가 속한 북클럽의 게시글만 검색
//            boards = boards.stream()
//                    .filter(board -> board.getBoardType() != BoardType.CLUB_BOARD || userBookClubIds.contains(board.getBookclubId()))
//                    .collect(Collectors.toList());
//
//            // 유저가 속한 북클럽 검색
//            bookClubs = bookClubRepository.findAll(Specification.where(BookClubSpecification.containsCategoryOrName(keyword))
//                    .and(BookClubSpecification.bookClubIdIn(userBookClubIds)));
//        } else {
//            // 인증되지 않은 유저는 일반 게시글과 피드가 검색가능
//            bookClubs = bookClubRepository.findAll(Specification.where(BookClubSpecification.containsCategoryOrName(keyword)));
//        }
//
//        // 3. 책 검색 추가
//        books = bookRepository.findAll(BookSpecification.containsTitleOrAuthorOrDescriptionOrCategory(keyword));
//
//        return new SearchResponse(boards, bookClubs, books);
//    }

}
