package com.readmate.ReadMate.bookclub.dailyMission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeResponse {
    private Long missionId;
    private LocalDate date;
    private int startPage; // 시작 페이지
    private int endPage;
    private String title;
    private String bookCover;
    private int progressPercentage;

}

