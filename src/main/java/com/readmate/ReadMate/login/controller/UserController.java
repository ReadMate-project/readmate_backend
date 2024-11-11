package com.readmate.ReadMate.login.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import com.readmate.ReadMate.image.utils.S3Uploader;
import com.readmate.ReadMate.login.dto.req.UserUpdateRequest;
import com.readmate.ReadMate.login.dto.res.BasicResponse;
import com.readmate.ReadMate.login.dto.req.KakaoLoginRequest;
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
//    @GetMapping("/oauth2/kakao/code")
    @PostMapping("/login/kakao")  // 프론트에서 인가코드 받아오는 url -> redirect로 프론트와 동일하게 설정해야한다.
    @Operation(summary = "회원가입 및 로그인", description = "유저의 정보가 있을 시 회원가입, 없을 시 로그인을 실시하는 API")
    public ResponseEntity<BasicResponse<String>> kakaoLogin(
            @RequestParam(name = "code") String code,
            @RequestBody(required = false) KakaoLoginRequest kakaoLoginRequest) {

        try {
            // code로 accessToken 획득
            String accessToken = userService.getKakaoAccessToken(code);

            // accessToken으로 유저 정보 가져오기
            User kakaoUser = userService.getKakaoUser(accessToken);

            User user = userService.findByEmail(kakaoUser.getEmail());

            if (user == null) {
                // 신규 사용자일 경우 회원가입
                if (kakaoLoginRequest == null) {
                    return ResponseEntity.badRequest()
                            .body(BasicResponse.ofFailure("Body 정보가 필요합니다.", HttpStatus.BAD_REQUEST));
                }

                String nickname = kakaoLoginRequest.getNickname();
                userService.validateNickname(nickname);

                kakaoUser.setNickname(nickname);
                kakaoUser.setFavoriteGenre(kakaoLoginRequest.getFavoriteGenre());

                if (kakaoUser.getProfileImageUrl() != null) {
                    File imageFile = downloadImageFromUrl(kakaoUser.getProfileImageUrl());
                    String s3ImageUrl = s3Uploader.upload(imageFile, "profile-images");
                    kakaoUser.setProfileImageUrl(s3ImageUrl); // S3 URL을 User 엔티티에 저장
                    imageFile.delete(); // 업로드 후 임시 파일 삭제
                }



                user = userService.signup(kakaoUser);
                String refreshToken = tokenService.createRefreshToken(user);
                tokenService.saveRefreshToken(user, refreshToken); // DB에 RefreshToken 저장

                BasicResponse<String> response = BasicResponse.ofSuccess("회원가입 성공");
                return ResponseEntity.ok(response);

            } else {
                // 기존 사용자일 경우 로그인 처리
                String refreshToken = tokenService.getRefreshToken(user);

                if (refreshToken == null) {
                    refreshToken = tokenService.createRefreshToken(user);
                    tokenService.saveRefreshToken(user, refreshToken); // DB에 RefreshToken 저장
                }

                //해당 토큰은 accessToken을 JWT로 변환해서 보내는 것이기에 보안에서 문제 없음
                String newAccessToken = tokenService.renewAccessToken(refreshToken);

                CustomUserDetails userDetails = new CustomUserDetails(user);
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);


                HttpHeaders headers = new HttpHeaders();
//                headers.add("Authorization", "Bearer " + newAccessToken);
                //헤더에 refresh와 access 함께 보내줌
                headers.set("Authorization", "Bearer " + newAccessToken + ", Refresh " + refreshToken);

                BasicResponse<String> response = BasicResponse.ofSuccess("로그인 성공");
                return ResponseEntity.ok().headers(headers).body(response);

            }

        } catch (Exception e) {
            BasicResponse<String> response = BasicResponse.ofFailure("처리 중 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
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