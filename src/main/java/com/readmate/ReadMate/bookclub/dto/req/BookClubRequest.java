package com.readmate.ReadMate.bookclub.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.readmate.ReadMate.common.genre.Genre;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.w3c.dom.Text;

import java.time.LocalDate;
import java.util.List;

@Data
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
    @Column(name = "recruitment_start_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Builder.Default
    private LocalDate recruitmentStartDate = LocalDate.now(); // Default to today's date


    @Column(name = "recruitment_end_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Builder.Default
    private LocalDate recruitmentEndDate = LocalDate.now().plusMonths(1); // Default to one month later

}
