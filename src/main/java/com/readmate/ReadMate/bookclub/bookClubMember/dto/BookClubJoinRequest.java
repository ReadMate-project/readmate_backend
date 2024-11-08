package com.readmate.ReadMate.bookclub.bookClubMember.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookClubJoinRequest {

    @NotNull
    private String joinMessage;


}
