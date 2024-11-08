package com.readmate.ReadMate.bookclub.club.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BookClubRequest {

    private String bookClubName;
    private String description;
    private Long leaderId;
    private List<BookInfoRequest> bookList;

    //진행 기간
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate endDate;

    // 모집 기간
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Builder.Default
    private LocalDate recruitmentStartDate = LocalDate.now(); // Default to today's date


    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Builder.Default
    private LocalDate recruitmentEndDate = LocalDate.now().plusMonths(1); // Default to one month later

}
