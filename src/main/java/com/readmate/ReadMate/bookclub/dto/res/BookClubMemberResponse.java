package com.readmate.ReadMate.bookclub.dto.res;

import com.readmate.ReadMate.bookclub.entity.BookClub;
import com.readmate.ReadMate.bookclub.entity.BookClubMemberRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookClubMemberResponse {
    private Long bookClubMemberId;
    private Long userId;
    @Enumerated(EnumType.STRING)
    private BookClubMemberRole clubMemberRole;
    private BookClub bookClub;
    private Boolean isApprove;
    private String joinMessage;
    private String delYn;
}
