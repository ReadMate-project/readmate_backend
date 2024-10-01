package com.readmate.ReadMate.bookclub.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyMissionCompletion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "daily_mission_id")
    private DailyMission dailyMission;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private BookClubMember member; // 미션을 완료한 멤버

    private LocalDate completionDate; // 미션 완료 날짜

    public BookClubMember getMember() {
        return member;
    }


}
