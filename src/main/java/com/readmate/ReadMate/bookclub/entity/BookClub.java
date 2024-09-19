package com.readmate.ReadMate.bookclub.entity;

import com.readmate.ReadMate.bookclub.dto.req.BookClubRequest;
import com.readmate.ReadMate.common.genre.Genre;
import com.readmate.ReadMate.common.genre.GenreConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookClub {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id")
    private Long bookClubId;

    @NotNull
    @Column(name = "club_name")
    private String bookClubName;

    @Column(name = "description")
    private String description;

    @Convert(converter = GenreConverter.class)
    @Column(name = "book_club_genre")
    private List<Genre> bookClubGenre;

    @NotNull
    @Column(name = "leader_id")
    private long leaderId;

    @Column(name = "club_image_id")
    private Long bookClubImageID;

    @Column
    private String notify;

    @NotNull
    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = true; // Default value as true

    @NotNull
    @Column(name = "start_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Builder.Default
    private LocalDate startDate = LocalDate.now(); // Default to today's date

    @Column(name = "end_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Builder.Default
    private LocalDate endDate = LocalDate.now().plusMonths(1); // Default to one month later

    @Column(name = "del_yn", columnDefinition = "VARCHAR(1) default 'N'")
    @Builder.Default
    private String delYn = "N";

    public void updateBookClub(BookClubRequest clubRequest){
        this.setBookClubName(clubRequest.getBookClubName());
        this.setLeaderId(clubRequest.getLeaderId());
        this.setIsPublic(clubRequest.isPublic());
        this.setBookClubImageID(clubRequest.getBookClubImageId());
        this.setDescription(clubRequest.getDescription());
        this.setNotify(clubRequest.getNotify());
        this.setStartDate(clubRequest.getStartDate() != null ? clubRequest.getStartDate() : LocalDate.now());
        this.setEndDate(clubRequest.getEndDate() != null ? clubRequest.getEndDate() : LocalDate.now().plusMonths(1));
        this.setBookClubGenre(clubRequest.getBookClubGenre());
    }

    public void delete(){
        this.delYn = "Y";
    }
}
