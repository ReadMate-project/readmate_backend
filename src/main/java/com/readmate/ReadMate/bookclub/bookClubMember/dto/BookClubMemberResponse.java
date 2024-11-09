package com.readmate.ReadMate.bookclub.bookClubMember.dto;

import com.readmate.ReadMate.bookclub.bookClubMember.entity.BookClubMemberRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Getter
public class BookClubMemberResponse {
    private Long bookClubMemberId;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private BookClubMemberRole clubMemberRole;

    private Long bookClubId;

    private Boolean isApprove;

    private String joinMessage;

    private boolean delYn;

}
