package com.readmate.ReadMate.bookclub.dto.res;

import com.readmate.ReadMate.book.entity.Book;
import com.readmate.ReadMate.bookclub.entity.BookClubMember;
import com.readmate.ReadMate.bookclub.repository.BookClubMemberRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Member;
import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionResponse {
    private Long missionId;
    private LocalDate date;
    private int todayPage;
    private String title;
    private String bookCover;

    private int progressPercentage;

}

