package com.readmate.ReadMate.bookclub.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.readmate.ReadMate.bookclub.entity.BookClub;
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

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class BookClubResponse {
    private Long bookClubId;
    private String bookClubName;
    private String description;
    private List<Genre> bookClubGenres;

    private long leaderId;
    private Long bookClubImageId;
    private String notify;

//    private boolean isPublic;

    private Long totalPage;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
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


    private List<BookClubBookResponse> bookClubBooks;

    private String bookClubKey;



    public void createBookClubResponse(BookClub bookClub,  List<BookClubBookResponse> bookClubBooks) {
        this.bookClubBooks = bookClubBooks;
        this.bookClubId = bookClub.getBookClubId();
        this.bookClubName = bookClub.getBookClubName();
        this.description = bookClub.getDescription();
        this.bookClubGenres = bookClub.getBookClubGenre();
        this.leaderId = bookClub.getLeaderId();
        this.bookClubImageId = bookClub.getBookClubImageID();
        this.startDate = bookClub.getStartDate();
        this.endDate = bookClub.getEndDate();
        this.notify = bookClub.getNotify();
//        this.isPublic = bookClub.getIsPublic();
        this.recruitmentEndDate = bookClub.getRecruitmentEndDate();
        this.recruitmentStartDate = bookClub.getRecruitmentStartDate();
        this.bookClubKey = bookClub.getBookClubKey();

    }


}
