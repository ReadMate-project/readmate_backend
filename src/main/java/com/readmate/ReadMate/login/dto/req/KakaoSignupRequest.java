package com.readmate.ReadMate.login.dto.req;

import com.readmate.ReadMate.common.genre.Genre;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoSignupRequest {
    private String email;
    private String nickname;
    private List<Genre> favoriteGenre;
}
