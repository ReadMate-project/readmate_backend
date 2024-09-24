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

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "bookclub_id") //CLUB_BOARD 때문에
    private Long bookclubId;

    //게시판 본문에 해당하는 내용
    //사진은 따로 Image로 구현할거임
    @Column(name = "content")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "title")
    private String title;

}
