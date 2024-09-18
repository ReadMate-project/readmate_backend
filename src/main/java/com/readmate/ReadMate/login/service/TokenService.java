package com.readmate.ReadMate.login.service;

import com.readmate.ReadMate.login.entity.RefreshToken;
import com.readmate.ReadMate.login.entity.User;
import com.readmate.ReadMate.login.repository.RefreshTokenRepository;
import com.readmate.ReadMate.login.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.secret.key}")
    private String secretKey;

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

    //RefreshToken 이용하여 AccessToken 재발급
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

    // AccessToken을 이용하여 유저 정보 조회
    public User getUserFromAccessToken(String accessToken) {
        try {
            // AccessToken에서 Claims(페이로드) 추출
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(accessToken)
                    .getBody();

            // Claims에서 이메일 추출
            String email = claims.getSubject();

            // 이메일로 유저 조회
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        } catch (SignatureException e) {
            // 토큰이 유효하지 않거나 서명이 일치하지 않을 경우 예외 처리
            throw new RuntimeException("Invalid access token");
        }
    }

////    // 2. 토큰에서 유저 정보 추출
////    public User getUserFromToken(String token) {
////        // 토큰 파싱해서 subject(이메일) 추출
////        Claims claims = Jwts.parser()
////                .setSigningKey(secretKey)  // secretKey를 사용하여 서명 검증
////                .parseClaimsJws(token)     // 토큰을 파싱
////                .getBody();                // claim에서 body 부분 추출
////
////        String email = claims.getSubject();  // 토큰의 subject에서 이메일 추출
////
////        return userRepository.findByEmail(email).orElseThrow(
////                () -> new IllegalArgumentException("유효하지 않은 토큰입니다.")
////        );
////    }
//
//    // 토큰에서 유저 정보 추출
//    public String getUserFromToken(String token) {
//        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
//    }

}