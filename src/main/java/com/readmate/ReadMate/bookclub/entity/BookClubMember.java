package com.readmate.ReadMate.bookclub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookClubMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookClubMemberId;
    private Long userId;

    @Enumerated(EnumType.STRING)
    private  BookClubMemberRole clubMemberRole;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_club_id")
    private BookClub bookClub;
    @Builder.Default
    private Boolean isApprove = Boolean.FALSE;
    private String joinMessage;
    @Column(name = "del_yn", columnDefinition = "VARCHAR(1) default 'N'")
    @Builder.Default
    private String delYn = "N";

    public BookClub getBookClub() {
        return bookClub;
    }


}
