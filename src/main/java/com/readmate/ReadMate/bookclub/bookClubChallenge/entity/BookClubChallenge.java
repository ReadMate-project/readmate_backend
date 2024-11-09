package com.readmate.ReadMate.bookclub.bookClubChallenge.entity;

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
    @Column(name = "book_club_id")
    private Long bookClubId;

    @NotNull
    @Column(name = "challenge_start_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull
    @Column(name = "challenge_end_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;


    // Book 엔티티의 isbn13을 참조하도록 변경
    @Column(name = "isbn13", nullable = false)
    private String isbn13;

    private int progressPercentage;

    @Column(name = "del_yn" )
    @Builder.Default
    private boolean delYn = false;

    @Builder
    public BookClubChallenge(Long challengeId, Long bookClubId, LocalDate startDate,
                             LocalDate endDate, String  isbn13, int progressPercentage) {
        this.challengeId = challengeId;
        this.bookClubId = bookClubId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isbn13 = isbn13;
        this.progressPercentage = progressPercentage;
    }
    public void delete(){
        this.delYn = true;
    }
}
