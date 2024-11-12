package com.readmate.ReadMate.bookclub.dailyMission.entity;

import com.readmate.ReadMate.bookclub.bookClubMember.entity.BookClubMember;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyMissionCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name = "daily_mission_id")
    private Long dailyMissionId;

    @Column(name = "user_id")
    private Long userId; // 미션을 완료한 멤버

    @Column(name = "completed_date")
    private LocalDate completionDate; // 미션 완료 날짜

}
