package com.readmate.ReadMate.login.dto.req;

import com.readmate.ReadMate.common.genre.Genre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class KakaoLoginRequest {

    private String code;  // 카카오 인증 코드 (front에서 주는 인증코드);\

    private String nickname;
    private String content;
    private List<Genre> favoriteGenre;
}