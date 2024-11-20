package com.readmate.ReadMate.like.service;

import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.board.entity.BoardType;
import com.readmate.ReadMate.board.service.BoardService;
import com.readmate.ReadMate.bookclub.bookClubMember.service.BookClubMemberService;
import com.readmate.ReadMate.common.exception.CustomException;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import com.readmate.ReadMate.like.entity.Likes;
import com.readmate.ReadMate.like.repository.LikesRepository;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikesSerivce {

    private final LikesRepository likesRepository;
    private final BoardService boardService;
    private final BookClubMemberService bookClubMemberService;

    @Transactional
    public Likes toggleLike(Long boardId, CustomUserDetails userDetails) {
        // 유저 인증 상태 확인
        if (userDetails == null || userDetails.getUser() == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 게시글 조회 및 존재 여부 확인
        Board board = boardService.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_BOARD));

        // 게시글 타입에 따른 처리
        if (board.getBoardType() == BoardType.CLUB_BOARD) {
            // CLUB_BOARD 타입인 경우 북클럽 회원 여부 확인
            Long bookclubId = board.getBookclubId();
            bookClubMemberService.findApprovedMember(bookclubId, userDetails.getUser().getUserId());
        }
        // BOARD 또는 FEED 타입인 경우 인증된 유저인지 확인만 하면 됨
        else if (board.getBoardType() == BoardType.BOARD || board.getBoardType() == BoardType.FEED) {
        } else {
            throw new CustomException(ErrorCode.INVALID_BOARD);
        }

        // 기존에 좋아요를 누른 기록이 있는지 조회
        Optional<Likes> existingLike = likesRepository.findByBoardIdAndUserId(boardId, userDetails.getUser().getUserId());

        if (existingLike.isPresent()) {
            Likes like = existingLike.get();

            if (like.getLiked()) {
                // 좋아요를 누른 상태 -> 좋아요 취소
                like.setLiked(false);
            } else {
                // 좋아요 취소 상태 -> 다시 좋아요
                like.setLiked(true);
            }

            likesRepository.save(like); // 상태 업데이트
            return like;

        } else {
            // 좋아요가 눌려 있지 않은 경우 -> 새로운 좋아요 생성
            Likes newLike = new Likes(null, boardId, userDetails.getUser().getUserId(), true);
            return likesRepository.save(newLike);
        }
    }


    public int countLikesByBoardId(Long boardId) {
        return likesRepository.countByBoardIdAndLikedTrue(boardId);
    }

    public List<Likes> getLikesByBoardId(Long boardId) {
        return likesRepository.findByBoardIdAndLikedTrue(boardId);
    }

    public List<Likes> getAllLikes() {
        return likesRepository.findAll();
    }

}
