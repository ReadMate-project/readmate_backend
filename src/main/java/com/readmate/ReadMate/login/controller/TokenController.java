package com.readmate.ReadMate.login.controller;

import com.readmate.ReadMate.login.dto.res.BasicResponse;
import com.readmate.ReadMate.login.dto.req.TokenRequest;
import com.readmate.ReadMate.login.entity.User;
import com.readmate.ReadMate.login.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
@Tag(name = "getUserInfo", description = "getUserInfomation API")
public class TokenController {

    private final TokenService tokenService;

    //accessToken 재발급 받고 그 토큰을 RequestBody에 넣어서 사용해야함
    @PostMapping("/user")
    @Operation(summary = "유저 정보 가지고오기", description = "access 토큰으로 user 정보 가지고 오는 API")
    public ResponseEntity<BasicResponse<User>> getUserFromToken(@RequestBody TokenRequest tokenRequest) {
        String accessToken = tokenRequest.getAccessToken();
        try {

            User user = tokenService.getUserFromAccessToken(accessToken);

            return ResponseEntity.ok(BasicResponse.ofSuccess(user));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(BasicResponse.ofFailure("유효하지 않는 토큰입니다.", HttpStatus.UNAUTHORIZED));
        }
    }
}
