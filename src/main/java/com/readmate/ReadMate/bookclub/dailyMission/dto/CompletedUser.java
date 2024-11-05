package com.readmate.ReadMate.bookclub.dailyMission.dto;

import lombok.Builder;

@Builder
public record CompletedUser(  long userId,  String userProfile ) {

}
