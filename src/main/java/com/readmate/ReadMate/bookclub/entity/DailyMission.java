package com.readmate.ReadMate.bookclub.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;

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
    private int pagesToRead;       // 읽어야 할 페이지 수

    @ManyToOne
    @JoinColumn(name = "challenge_id")
    private BookClubChallenge challenge; // 어떤 챌린지의 미션인지


}
