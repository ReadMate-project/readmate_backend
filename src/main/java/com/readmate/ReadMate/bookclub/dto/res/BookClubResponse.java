package com.readmate.ReadMate.bookclub.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.readmate.ReadMate.bookclub.entity.BookClub;
import com.readmate.ReadMate.common.genre.Genre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private boolean isPublic;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private List<BookClubBookResponse> bookClubBooks;



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
        this.isPublic = bookClub.getIsPublic();
    }


}
