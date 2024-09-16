package com.readmate.ReadMate.login.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KakaoLoginResponse {

    private String accessToken;   // JWT 액세스 토큰
    private String refreshToken;  // 리프레시 토큰
}
