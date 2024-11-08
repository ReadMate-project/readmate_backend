package com.readmate.ReadMate.bookclub.bookClubChallenge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final BookClubChallengeService bookClubChallengeService;

    @Scheduled(cron = "0 0 0 * * *")
    public void run(){
        bookClubChallengeService.scheduleService();
    }

}
