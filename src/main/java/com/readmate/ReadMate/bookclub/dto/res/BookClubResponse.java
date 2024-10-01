package com.readmate.ReadMate.bookclub.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.readmate.ReadMate.book.entity.Book;
import com.readmate.ReadMate.bookclub.entity.BookClub;
import com.readmate.ReadMate.bookclub.entity.BookClubChallenge;
import com.readmate.ReadMate.common.genre.Genre;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.w3c.dom.Text;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class BookClubResponse {
    private Long bookClubId;
    private String bookClubName;
    private String description;
    private long leaderId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private String bookClubKey;

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
    private List<BookClubChallengeResponse> challenges;


    public void createBookClubResponse(BookClub bookClub, List<BookClubChallengeResponse> challenges) {
        this.bookClubId = bookClub.getBookClubId();
        this.bookClubName = bookClub.getBookClubName();
        this.description = bookClub.getDescription();
        this.leaderId = bookClub.getLeaderId();
        this.startDate = bookClub.getStartDate();
        this.endDate = bookClub.getEndDate();
        this.recruitmentEndDate = bookClub.getRecruitmentEndDate();
        this.recruitmentStartDate = bookClub.getRecruitmentStartDate();
        this.bookClubKey = bookClub.getBookClubKey();
        this.challenges = challenges;

    }


}
