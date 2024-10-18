package com.readmate.ReadMate.bookclub.entity;

import com.readmate.ReadMate.bookclub.dto.req.BookClubRequest;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.w3c.dom.Text;

import java.time.LocalDate;
import java.util.List;

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

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "leader_id")
    @NotNull
    private Long leaderId;

    @NotNull
    @Column(name = "start_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Builder.Default
    private LocalDate startDate = LocalDate.now().plusDays(8); // Default to today's date

    @Column(name = "end_date")
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Builder.Default
    private LocalDate endDate = LocalDate.now().plusMonths(2); // Default to one month later


    // 모집 기간
    @NotNull
    @Column(name = "recruitment_start_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Builder.Default
    private LocalDate recruitmentStartDate = LocalDate.now(); // Default to today's date


    @Column(name = "recruitment_end_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Builder.Default
    @NotNull
    private LocalDate recruitmentEndDate = LocalDate.now().plusWeeks(1); // Default to one month later

    @Column(name = "del_yn", columnDefinition = "VARCHAR(1) default 'N'")
    @Builder.Default
    @NotNull
    private String delYn = "N";

    @Column(name = "book_club_key")
    @NotNull
    private String bookClubKey;

    @OneToMany(mappedBy = "bookClub", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BookClubChallenge> challenges;

    
    //인기 있는 북클럽 정렬을 위해 컬럼 추가
    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    public void createBookClub(BookClubRequest clubRequest){
        this.setBookClubName(clubRequest.getBookClubName());
        this.setDescription(clubRequest.getDescription());
        this.setStartDate(clubRequest.getStartDate() != null ? clubRequest.getStartDate() : LocalDate.now());
        this.setEndDate(clubRequest.getEndDate() != null ? clubRequest.getEndDate() : LocalDate.now().plusMonths(1));
        this.setRecruitmentStartDate(clubRequest.getRecruitmentStartDate() != null ? clubRequest.getRecruitmentStartDate() : LocalDate.now());
        this.setRecruitmentEndDate(clubRequest.getRecruitmentEndDate() != null ? clubRequest.getRecruitmentEndDate() : LocalDate.now().plusWeeks(1));

    }

    public void updateBookClub(BookClubRequest clubRequest){
        this.setBookClubName(clubRequest.getBookClubName());
        this.setLeaderId(clubRequest.getLeaderId());
        this.setDescription(clubRequest.getDescription());
        this.setStartDate(clubRequest.getStartDate() != null ? clubRequest.getStartDate() : LocalDate.now());
        this.setEndDate(clubRequest.getEndDate() != null ? clubRequest.getEndDate() : LocalDate.now().plusMonths(1));
        this.setRecruitmentStartDate(clubRequest.getRecruitmentStartDate() != null ? clubRequest.getRecruitmentStartDate() : LocalDate.now());
        this.setRecruitmentEndDate(clubRequest.getRecruitmentEndDate() != null ? clubRequest.getRecruitmentEndDate() : LocalDate.now().plusWeeks(1));
    }

    public void delete(){
        this.delYn = "Y";
    }




}
