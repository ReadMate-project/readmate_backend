package com.readmate.ReadMate.login.dto.req;

import com.readmate.ReadMate.common.genre.Genre;
import lombok.Data;

import java.util.List;

@Data
public class UserUpdateRequest { //마이페이지 내정보 수정시 사용

    private String nickname;
    private List<Genre> favoriteGenre;
    private String content;
}
