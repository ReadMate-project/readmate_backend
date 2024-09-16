package com.readmate.ReadMate.login.dto;

import com.readmate.ReadMate.login.entity.FavoriteGenre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class KakaoLoginRequest {

    private String code;  // 카카오 인증 코드 (front에서 주는 인증코드);\

    private String nickname;
    private String content;
    private FavoriteGenre favoriteGenre;
}