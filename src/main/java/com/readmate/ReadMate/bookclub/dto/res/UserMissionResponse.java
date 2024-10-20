package com.readmate.ReadMate.bookclub.dto.res;


import lombok.*;

import java.time.LocalDate;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class UserMissionResponse {
    private Long missionId;
    private Long bookClubId;
    private Long challengeId;
    private LocalDate date;
    private int startPage;
    private int endPage;
    private String title;
    private String bookCover;

}

