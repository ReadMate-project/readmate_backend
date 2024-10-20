package com.readmate.ReadMate.bookclub.repository;



import com.readmate.ReadMate.bookclub.entity.BookClubMember;
import com.readmate.ReadMate.bookclub.entity.DailyMission;
import com.readmate.ReadMate.bookclub.entity.DailyMissionCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;


public interface DailyMissionCompletionRepository extends JpaRepository<DailyMissionCompletion,Long> {
    @Query("SELECT d FROM DailyMissionCompletion d WHERE d.dailyMission.id = :dailyMissionId AND d.completionDate = :date")
    List<DailyMissionCompletion> findAllByDailyMissionIdAndCompletionDate(@Param("dailyMissionId") Long dailyMissionId, @Param("date") LocalDate date);
    boolean existsByDailyMissionAndMember(DailyMission dailyMission, BookClubMember member);

}
