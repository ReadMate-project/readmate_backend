package com.readmate.ReadMate.bookclub.dailyMission.repository;


import com.readmate.ReadMate.bookclub.dailyMission.entity.DailyMissionCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyMissionCompletionRepository extends JpaRepository<DailyMissionCompletion,Long> {
    List<DailyMissionCompletion> findAllByDailyMissionIdAndCompletionDate(Long dailyMissionId, LocalDate completionDate);
    boolean existsByDailyMissionIdAndUserId(final long dailyMissionId, final long userId);

    void deleteByDailyMissionId(Long missionId);
}
