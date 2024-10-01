package com.readmate.ReadMate.bookclub.repository;

import com.readmate.ReadMate.bookclub.dto.res.MissionResponse;
import com.readmate.ReadMate.bookclub.entity.BookClubChallenge;
import com.readmate.ReadMate.bookclub.entity.DailyMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyMissionRepository extends JpaRepository<DailyMission,Long> {

    DailyMission findByChallengeAndMissionDate(BookClubChallenge challenge, LocalDate date);

    List<DailyMission> findAllByChallenge(BookClubChallenge challenge);

    List<DailyMission> findAllByChallengeAndMissionDate(BookClubChallenge challenge, LocalDate missionDate);


}
