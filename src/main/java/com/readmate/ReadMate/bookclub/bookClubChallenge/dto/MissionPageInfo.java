package com.readmate.ReadMate.bookclub.bookClubChallenge.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MissionPageInfo {
    private int pagesPerDay;
    private int remainder;

    public void decrementRemainder() {
        if (remainder > 0) {
            remainder--;
        }
    }
}