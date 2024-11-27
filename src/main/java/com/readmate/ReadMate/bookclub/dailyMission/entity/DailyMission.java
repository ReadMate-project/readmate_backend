package com.readmate.ReadMate.bookclub.dailyMission.entity;

import com.readmate.ReadMate.bookclub.bookClubChallenge.entity.BookClubChallenge;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long missionId;

    private LocalDate missionDate; // 미션 날짜

    @Column(name = "start_page")
    private int startPage; // 시작 페이지

    @Column(name = "end_page")
    private int endPage; // 끝 페이지

    @Column(name = "challenge_id")
    private Long challengeId; // 어떤 챌린지의 미션인지

}
