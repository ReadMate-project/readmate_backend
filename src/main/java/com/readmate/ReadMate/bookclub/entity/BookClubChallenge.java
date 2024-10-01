package com.readmate.ReadMate.bookclub.entity;

import com.readmate.ReadMate.book.entity.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookClubChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long challengeId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_club_id")
    private BookClub bookClub;

    @NotNull
    @Column(name = "challenge_start_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull
    @Column(name = "challenge_end_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    private int progressPercentage;

    @Column(name = "del_yn", columnDefinition = "VARCHAR(1) default 'N'")
    @Builder.Default
    private String delYn = "N";

    @Builder
    public BookClubChallenge(Long challengeId, BookClub bookClub, LocalDate startDate,
                             LocalDate endDate, Book book, int progressPercentage) {
        this.challengeId = challengeId;
        this.bookClub = bookClub;
        this.startDate = startDate;
        this.endDate = endDate;
        this.book = book;
        this.progressPercentage = progressPercentage;
    }
    public void delete(){
        this.delYn = "Y";
    }
}
