package com.readmate.ReadMate.book.entity;

import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.login.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class MyBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long myBookId;

    @Column(name = "user_id", nullable = false)
    private Long userId;  // 어떤 사용자인지 저장

    @Column(name = "isbn13", nullable = false)
    private String isbn13;  // Book 엔티티의 isbn13을 참조

    @Column(name = "last_read_date")
    private LocalDate lastReadDate;

    @Column(name = "del_yn")
    @Builder.Default
    private boolean delYn = false;

}
