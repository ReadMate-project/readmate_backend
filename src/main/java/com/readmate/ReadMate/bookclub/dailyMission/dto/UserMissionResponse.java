package com.readmate.ReadMate.bookclub.dailyMission.dto;


import lombok.*;

import java.time.LocalDate;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class UserMissionResponse {
    private Long missionId;
    private String bookClubTittle;
    private LocalDate date;
    private int startPage;
    private int endPage;
    private String title;
    private String bookCover;
}

