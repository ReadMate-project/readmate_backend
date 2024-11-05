package com.readmate.ReadMate.bookclub.dailyMission.repository;

import com.readmate.ReadMate.bookclub.bookClubChallenge.entity.BookClubChallenge;
import com.readmate.ReadMate.bookclub.dailyMission.entity.DailyMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyMissionRepository extends JpaRepository<DailyMission,Long> {

    List<DailyMission> findAllByChallengeId(Long challengeId);

   Optional<DailyMission> findByChallengeIdAndMissionDate(long challengeId, LocalDate today);
}
