package com.readmate.ReadMate.bookclub.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.readmate.ReadMate.common.genre.Genre;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BookClubRequest {

    private String bookClubName;
    private String description;
    private List<Genre> bookClubGenre;
    private long leaderId;
    private Long bookClubImageId;
    private String notify;

    private boolean isPublic;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate endDate;

    private List<Long> bookList;
}
