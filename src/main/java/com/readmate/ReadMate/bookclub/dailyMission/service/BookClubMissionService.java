package com.readmate.ReadMate.bookclub.dailyMission.service;


import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.board.service.BoardService;
import com.readmate.ReadMate.book.dto.res.BookResponse;
import com.readmate.ReadMate.book.service.BookService;
import com.readmate.ReadMate.bookclub.bookClubChallenge.dto.MissionPageInfo;
import com.readmate.ReadMate.bookclub.bookClubChallenge.entity.BookClubChallenge;
import com.readmate.ReadMate.bookclub.bookClubMember.service.BookClubMemberService;
import com.readmate.ReadMate.bookclub.dailyMission.dto.CompletedUser;
import com.readmate.ReadMate.bookclub.dailyMission.entity.DailyMission;
import com.readmate.ReadMate.bookclub.dailyMission.entity.DailyMissionCompletion;
import com.readmate.ReadMate.bookclub.dailyMission.repository.DailyMissionCompletionRepository;
import com.readmate.ReadMate.bookclub.dailyMission.repository.DailyMissionRepository;
import com.readmate.ReadMate.common.exception.CustomException;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import com.readmate.ReadMate.login.entity.User;
import com.readmate.ReadMate.login.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BookClubMissionService {

    private final DailyMissionRepository dailyMissionRepository;
    private final BookService bookService;
    private final DailyMissionCompletionRepository dailyMissionCompletionRepository;
    private final BookClubMemberService bookClubMemberService;
    private final UserService userService;
    private final BoardService boardService;

    public DailyMission getByChallengeAndDate(final long challengeId, final LocalDate today){
        return dailyMissionRepository.findByChallengeIdAndMissionDate(challengeId, today)
                .orElseThrow (()->new CustomException(ErrorCode.INVALID_MISSION));

    }

    public void deleteByChallengeId(final long  challengeId) {
        List<DailyMission> dailyMissionList = dailyMissionRepository.findAllByChallengeId(challengeId);
        dailyMissionRepository.deleteAll(dailyMissionList);
    }

    public void createMissionsForChallenge(final BookClubChallenge challenge) {

        LocalDate startDate = challenge.getStartDate();
        LocalDate endDate = challenge.getEndDate();
        BookResponse book = bookService.getBookByIsbn(challenge.getIsbn13());
        Long totalPages = book.getTotalPages();

        MissionPageInfo missionPageInfo = calculatePagesPerDay(totalPages, startDate, endDate);
        List<DailyMission> dailyMissions = generateDailyMission(challenge.getChallengeId(), startDate, endDate, missionPageInfo);
        System.out.println("dailyMissions = " + dailyMissions);
        dailyMissionRepository.saveAll(dailyMissions);
    }


    // 하루에 읽어야 할 페이지 수와 나머지 페이지 수 계산
    private MissionPageInfo calculatePagesPerDay(Long totalPages, LocalDate startDate, LocalDate endDate) {
        int days = (int) (endDate.toEpochDay() - startDate.toEpochDay() + 1);
        int pagesPerDay = (int) (totalPages / days);
        int remainder = (int) (totalPages % days);
        return new MissionPageInfo(pagesPerDay, remainder);
    }

    private List<DailyMission> generateDailyMission(final long bookClubChallengeId ,LocalDate startDate, LocalDate endDate, MissionPageInfo missionPageInfo){
        List<DailyMission> dailyMissions = new ArrayList<>();
        int currentPage =1;

        //startDate ~ endDate 까지 미션 생성
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)){
            int endPage = currentPage + missionPageInfo.getPagesPerDay()-1; //읽어야할 페이지 계산
            // 나머지 페이지 수가 남아있는 경우 하루에 읽어야할 페이지 +1 증가 또는 감소
            if(missionPageInfo.getRemainder() > 0){
                endPage +=1;
                missionPageInfo.decrementRemainder();
            }
            // DailyMission 생성
            dailyMissions.add(DailyMission.builder()
                    .missionDate(date)
                    .startPage(currentPage)
                    .endPage(endPage)
                    .challengeId(bookClubChallengeId)
                    .build());

            currentPage = endPage + 1; //현재 페이지를 endPage +1
        }
        return dailyMissions;

        }

    public List<CompletedUser> getCompletedMembers(final long bookClubId, final long userId, final long  dailyMissionId, LocalDate date) {

        // 권한이 있는지 확인
        bookClubMemberService.validateApprovedMember(bookClubId, userId);

        // 완료한 유저 리스트 받아옴
        List<DailyMissionCompletion> missionCompletions = dailyMissionCompletionRepository
                .findAllByDailyMissionIdAndCompletionDate(dailyMissionId, date);

        // USER ID 리스트로 반환
        return missionCompletions.stream()
                .map(completion -> {
                    // 각 userId에 대해 userProfile 정보 조회
                    User user = userService.findByUserId(completion.getUserId()); // userId를 사용하여 프로필 정보 조회
                    return new CompletedUser(
                            user.getUserId(),  // userId 매핑
                            user.getProfileImageUrl(),           // userProfile 매핑
                            user.getNickname()

                    );
                })
                .collect(Collectors.toList());
    }

    public void completeMission(final long dailyMissionId, final long  userId, final long  boardId) {
        // 미션 찾기
        DailyMission dailyMission = dailyMissionRepository.findById(dailyMissionId)
                .orElseThrow(() -> new RuntimeException("Mission not found"));

        Board board = boardService.getBoardById(boardId);

        // 해당 유저가 이미 이 미션을 완료했는지 확인
        boolean alreadyCompleted = dailyMissionCompletionRepository.existsByDailyMissionIdAndUserId(dailyMissionId, userId);

        // 이미 미션을 완료했으면 에러 메시지
        if (alreadyCompleted) {
            throw new CustomException(ErrorCode.MISSION_ALREADY_COMPLETED); // 적절한 에러 메시지를 추가
        }

        // DailyMission의 날짜와 게시글의 작성 날짜가 일치하는지 확인
        if (!dailyMission.getMissionDate().equals(board.getCreatedAt().toLocalDate())) {
            throw new CustomException(ErrorCode.MISSION_DATE_MISMATCH);  // 미션 날짜와 게시글 작성 날짜가 다를 경우 예외 처리
        }

        // 미션 완료 처리
        DailyMissionCompletion completion = DailyMissionCompletion.builder()
                .dailyMissionId(dailyMissionId)
                .userId(userId)
                .completionDate(LocalDateTime.now())  // 현재 날짜로 완료 날짜 설정
                .build();

        // 완료된 미션 저장
        dailyMissionCompletionRepository.save(completion);
    }
}







