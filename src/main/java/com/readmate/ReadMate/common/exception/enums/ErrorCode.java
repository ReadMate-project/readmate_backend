package com.readmate.ReadMate.common.exception.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // JWT
    NOT_MATCHING_TOKEN (401, "JWT001", "토큰이 일치하지 않습니다."),
    NO_REFRESH_TOKEN (400, "JWT002", "refresh 토큰이 존재하지 않습니다."),
    INVALID_TOKEN(403, "JWT003", "유효하지 않은 토큰입니다."),
    NOT_MATCHING_REFRESH_TOKEN(403, "JWT004", "저장된 refresh 토큰과 일치하지 않습니다."),

    //USER
    ALREADY_EXIST_USER(409, "USER001", "이미 존재하는 사용자입니다."),
    INVALID_USER(404, "USER002", "존재하지 않는 사용자입니다."),
    WRONG_PASSWORD(HttpStatus.UNAUTHORIZED.value(), "USER003", "잘못된 비밀번호입니다."),

    // ROLE
    ACCESS_DENIED (403, "ROLE001", "접근 권한이 없습니다."),

    // BOARD
    INVALID_BOARD(404, "BOARD001", "존재하지 않는 게시물입니다."),
    ACTIVE_BOARD(403, "BOARD002", "삭제되지 않은 게시물입니다. 복구할 필요 없습니다."),
    ALREADY_LIKED(409, "LIKE001", "이미 좋아요를 누른 게시물입니다."),
    NOT_LIKED(403, "LIKE002", "좋아요를 누르지 않은 게시물입니다. 삭제할 수 없습니다."),


    // BOOKCLUB
    INVALID_CLUB(404, "CLUB001", "존재하지 않는 클럽입니다."),

    INVALID_GENRE_FORMAT(501,"CLUB002" , "변환에 실패 했습니다"),

    ALREADY_DELETED(409, "CLUB003", "이미 삭제된 클럽입니다."),

    ALREADY_JOINED(409, "CLUB004", "이미 가입한 클럽입니다."),
    NOT_MEMBER(409, "CLUB005", "가입하지 않은 클럽입니다."),
    INVALID_LEAVE(408, "CLUB006", "리더는 탈퇴할 수 없습니다.리더를 수정해주세요"),
    INVALID_MEMBER(409, "CLUB007", "가입하지 않은 멤버 입니다"),
    INVALID_BOOK_DATE(409,"CLUB008","날짜를 잘못 선택하셨습니다"),

    //BOOK

    INVALID_BOOK(404, "BOOK001", "존재하지 않는 책 입니다."),
    API_CALL_FAILED(500,"BOOK002","Aladin API 에서 받아오는 것을 실패했습니다"),

    INVALID_GENRE(404, "BOOK003", "존재하지 않는 장르 입니다."),

    BOOK_NOT_FOUND(404, "BOOK004", "책을 찾을 수 없습니다"),


    // MISSION
    INVALID_MISSION(404, "MISSION001", "존재하지 않는 미션입니다."),


    // COMMENT
    INVALID_COMMENT(404, "BOOK004", "책을 찾을 수 없습니다.");


    private int status;
    private final String code;
    private final String message;
}
