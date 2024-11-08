package com.readmate.ReadMate.bookclub.bookClubMember.entity;

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
    private BookClubMemberRole clubMemberRole;

    @Getter
    @NotNull
    @Column(name = "book_club_id")
    private Long bookClubId;

    @Builder.Default
    private Boolean isApprove = Boolean.FALSE;

    private String joinMessage;

    @Column(name = "del_yn")
    @Builder.Default
    private boolean delYn= false;

}
