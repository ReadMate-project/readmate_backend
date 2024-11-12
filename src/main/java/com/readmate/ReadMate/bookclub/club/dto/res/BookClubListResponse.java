package com.readmate.ReadMate.bookclub.club.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.readmate.ReadMate.common.genre.Genre;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookClubListResponse {

    private Long bookClubID;
    private String bookClubName;
    private String description;
    private String bookCover;
    private List<Genre> favoriteGenre;

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
