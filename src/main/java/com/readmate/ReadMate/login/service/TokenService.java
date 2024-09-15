package com.readmate.ReadMate.login.service;

import com.readmate.ReadMate.login.entity.RefreshToken;
import com.readmate.ReadMate.login.entity.User;
import com.readmate.ReadMate.login.repository.RefreshTokenRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenService { //JWT 기반의 인증 및 토큰 관리를 담당

    private final RefreshTokenRepository refreshTokenRepository;
    private final String secretKey = "5E7EA62366FA799E66349E82FBAB7"; //JWT secretKey

    // AccessToken 생성
    public String createAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) //1시간
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // RefreshToken 생성
    public String createRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 3600000)) //1주일
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // RefreshToken 저장
    public void saveRefreshToken(User user, String refreshToken) {
        RefreshToken token = new RefreshToken();
        token.setRefreshToken(refreshToken);
        token.setUser(user);
        refreshTokenRepository.save(token);
    }

    //RefreshToken이용하여 AccessToken 재발급
    public String renewAccessToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByRefreshToken(refreshToken).orElse(null);
        if (token == null) {
            return null;
        }
        User user = token.getUser();
        return createAccessToken(user);
    }

    // RefreshToken 조회
    public String getRefreshToken(User user) {
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user);
        if (refreshToken != null) {
            return refreshToken.getRefreshToken();
        }
        return null;
    }
}