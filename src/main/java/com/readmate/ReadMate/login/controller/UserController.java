package com.readmate.ReadMate.login.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import com.readmate.ReadMate.image.utils.S3Uploader;
import com.readmate.ReadMate.login.dto.req.KakaoSignupRequest;
import com.readmate.ReadMate.login.dto.req.UserUpdateRequest;
import com.readmate.ReadMate.login.dto.res.BasicResponse;
import com.readmate.ReadMate.login.dto.req.KakaoTokenRequest;
import com.readmate.ReadMate.login.entity.User;
import com.readmate.ReadMate.login.repository.UserRepository;
import com.readmate.ReadMate.login.security.CustomUserDetails;
import com.readmate.ReadMate.login.service.TokenService;
import com.readmate.ReadMate.login.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication API")
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;


    @Value("${kakao.admin-key}")
    private String adminKey;


    //1. 카카오톡 회원가입 및 로그인
    //1.1 DB에 해당 유저의 정보가 없을 경우 -> 회원가입절차로 DB에 유저 정보 저장
    //1.2 DB에 해당 유저의 정보가 있을 경우 -> 로그인
    @PostMapping("/login/kakao")
    @Operation(summary = "카카오 로그인", description = "이메일 유무에 따라 회원가입 또는 로그인을 처리")
    public ResponseEntity<BasicResponse<Object>> kakaoLogin(
            @RequestParam(name = "code") String code) {

        try {
            String accessToken = userService.getKakaoAccessToken(code);
            User kakaoUser = userService.getKakaoUser(accessToken);
            User user = userService.findByEmail(kakaoUser.getEmail());

            if (user == null) {
                //1차적 회원가입 -> 카카오에서 주는 정보 저장
                User newUser = new User();
                newUser.setKakaoId(kakaoUser.getKakaoId());
                newUser.setEmail(kakaoUser.getEmail());

                if (kakaoUser.getProfileImageUrl() != null) {
                    File imageFile = downloadImageFromUrl(kakaoUser.getProfileImageUrl());
                    String s3ImageUrl = s3Uploader.upload(imageFile, "profile-images");
                    newUser.setProfileImageUrl(s3ImageUrl);
                    imageFile.delete();
                }

                userService.signup(newUser);

                Map<String, String> responseData = new HashMap<>();
                responseData.put("email", kakaoUser.getEmail());

                return ResponseEntity.status(HttpStatus.OK)
                        .body(BasicResponse.ofSuccess(responseData, "회원가입이 필요합니다."));
            } else {
                // 기존 사용자 -> 로그인 처리
                String refreshToken = tokenService.getRefreshToken(user);

                if (refreshToken == null) {
                    refreshToken = tokenService.createRefreshToken(user);
                    tokenService.saveRefreshToken(user, refreshToken); // DB에 RefreshToken 저장
                }

                String newAccessToken = tokenService.renewAccessToken(refreshToken);

                CustomUserDetails userDetails = new CustomUserDetails(user);
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 응답 헤더에 토큰 추가
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + newAccessToken + ", Refresh " + refreshToken);

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(BasicResponse.ofSuccess("로그인 성공"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BasicResponse.ofFailure("처리 중 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }


    @PostMapping("/signup/kakao")
    @Operation(summary = "카카오 회원가입", description = "닉네임과 좋아하는 장르를 추가로 저장하여 회원가입을 완료합니다.")
    public ResponseEntity<BasicResponse<String>> kakaoSignup(
            @RequestBody KakaoSignupRequest kakaoSignupRequest) {

        try {
            User user = userService.findByEmail(kakaoSignupRequest.getEmail());

            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(BasicResponse.ofFailure("사용자를 찾을 수 없습니다. 먼저 로그인 과정을 진행하세요.", HttpStatus.BAD_REQUEST));
            }

            user.setNickname(kakaoSignupRequest.getNickname());
            user.setFavoriteGenre(kakaoSignupRequest.getFavoriteGenre());
            userService.updateUser(user);

            String refreshToken = tokenService.createRefreshToken(user);
            tokenService.saveRefreshToken(user, refreshToken);

            return ResponseEntity.ok(BasicResponse.ofSuccess("회원가입이 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BasicResponse.ofFailure("회원가입 처리 중 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    private File downloadImageFromUrl(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        File file = File.createTempFile("temp", ".jpg");
        try (InputStream in = url.openStream(); FileOutputStream out = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return file;
    }


    // 2. 로그아웃
    @Operation(summary = "로그아웃", description = "로그아웃 API")
    @PostMapping("/logout")
    public ResponseEntity<BasicResponse<String>> logout(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String accessToken = bearerToken.substring(7); // "Bearer " 부분 제거
            userService.logout(accessToken);
        }

        BasicResponse<String> response = BasicResponse.ofSuccess("로그아웃 성공");
        return ResponseEntity.ok(response);
    }


    // 3. 회원탈퇴 요청 처리
    @Operation(summary = "회원탈퇴", description = "카카오+DB에서 유저탈퇴 API")
    @PostMapping("/withdraw")
    public ResponseEntity<BasicResponse<String>> withdraw(HttpServletRequest request) {
        String userIdHeader = request.getHeader("userId"); // userId를 통해 kakaoId를 가지고 오도록

        String authorizationHeader = "KakaoAK " + adminKey;

        if (userIdHeader != null) {
            Long userId = Long.valueOf(userIdHeader);

            //유저 정보 조회 (=> userId를 통해 kakaoId를 가져옴)
            Optional<User> optionalUser = userRepository.findByUserId(userId);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                Long kakaoId = user.getKakaoId();

                userService.withdraw(authorizationHeader, userId);

                BasicResponse<String> response = BasicResponse.ofSuccess("회원탈퇴 성공");
                return ResponseEntity.ok(response);

            } else {
                BasicResponse<String> response = BasicResponse.ofFailure("유저를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } else {
            BasicResponse<String> response = BasicResponse.ofFailure("유저 ID가 필요합니다.", HttpStatus.BAD_REQUEST);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    // 4. refreshToken으로 accessToken 재발급
    @PostMapping("/user/refresh")
    public ResponseEntity<BasicResponse<String>> refreshAccessToken(@RequestBody KakaoTokenRequest kakaoTokenRequest) {
        String refreshToken = kakaoTokenRequest.getRefreshToken();
        String newAccessToken = tokenService.renewAccessToken(refreshToken);

        if (newAccessToken == null) {
            BasicResponse<String> response = BasicResponse.ofFailure("error code : JWT003, 유효하지 않은 Refresh Token입니다.", HttpStatus.UNAUTHORIZED);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        BasicResponse<String> response = BasicResponse.ofSuccess("새로운 AccessToken: " + newAccessToken);
        return ResponseEntity.ok(response);
    }


    //5. 로그인 된 유저 정보 가지고 오기
    @GetMapping("/userInfo")
    @Operation(summary = "현재 사용자 정보 가져오기", description = "현재 인증된 사용자 정보를 가져오는 API")
    public ResponseEntity<BasicResponse<User>> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {

        //인증된 사용자 정보 가지고 오기
        User user = userDetails.getUser();
        System.out.println("userDetail: " + userDetails.getUser());
        return ResponseEntity.ok(BasicResponse.ofSuccess(user));
    }


    //6. 마이페이지 -> 유저정보 수정
    @PatchMapping(value = "/user/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BasicResponse<String>> updateUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "userUpdateRequest", required = false) String userUpdateRequestJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        try {
            User user = userDetails.getUser();

            // JSON 문자열이 있을 경우에만 UserUpdateRequest 객체로 변환
            if (userUpdateRequestJson != null && !userUpdateRequestJson.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                UserUpdateRequest userUpdateRequest = objectMapper.readValue(userUpdateRequestJson, UserUpdateRequest.class);

                // 닉네임 중복 체크
                if (!user.getNickname().equals(userUpdateRequest.getNickname()) &&
                        userService.isNicknameDuplicate(userUpdateRequest.getNickname())) {
                    BasicResponse<String> response = BasicResponse.ofFailure(ErrorCode.DUPLICATE_NICKNAME.getMessage(), HttpStatus.CONFLICT);
                    return new ResponseEntity<>(response, HttpStatus.CONFLICT);
                }

                user.setNickname(userUpdateRequest.getNickname());
                user.setFavoriteGenre(userUpdateRequest.getFavoriteGenre());
                user.setContent(userUpdateRequest.getContent());
            }

            // 프로필 이미지가 있을 경우에만 업데이트
            if (profileImage != null && !profileImage.isEmpty()) {
                String newImageUrl = userService.updateUserProfileImage(user, profileImage);
                user.setProfileImageUrl(newImageUrl); // S3 URL을 User 엔티티에 저장
            }

            userService.updateUser(user);

            BasicResponse<String> response = BasicResponse.ofSuccess("유저 정보 수정 성공");
            return ResponseEntity.ok(response);
        } catch (JsonProcessingException e) {
            BasicResponse<String> response = BasicResponse.ofFailure("JSON 파싱 오류 발생", HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            BasicResponse<String> response = BasicResponse.ofFailure("파일 처리 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            BasicResponse<String> response = BasicResponse.ofFailure("처리 중 오류 발생: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }
}