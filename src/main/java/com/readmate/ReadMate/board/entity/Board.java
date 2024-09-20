package com.readmate.ReadMate.board.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long BoardId;

    @Column(name = "user_id")
    private Long userId; // 알단 user와의 관계 설정 없이 사용 (=>ERD와 같이)

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private BoardType boardType;

    // isbn13를 저장할건가,,?
    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "bookclub_id") //CLUB_BOARD때문에 그런듯
    private Long bookclubId;

    @Column(name = "total_pages") //알라딘에서 제공해주는 총 페이지 가지고 와서 사용할 것
    private Integer totalPages;

    @Column(name = "current_page") //내가 작성할 부분
    private Integer currentPage;

    //게시판 본문에 해당하는 내용
    //사진은 따로 Image로 구현할거임
    @Column(name = "content")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "title")
    private String title;

    @Column(name = "likes") //게시글에 좋아요 누른 총 수
    private Long likes;

}
