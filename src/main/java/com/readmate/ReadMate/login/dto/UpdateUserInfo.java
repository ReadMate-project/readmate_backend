package com.readmate.ReadMate.login.dto;

import com.readmate.ReadMate.common.genre.Genre;

public class UpdateUserInfo { //마이페이지 -> 정보 수정 시 사용
    private String nickname;
    private Genre favoriteGenre;
    private String profileImageUrl;
    private String content;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Genre getFavoriteGenre() {
        return favoriteGenre;
    }

    public void setFavoriteGenre(Genre favoriteGenre) {
        this.favoriteGenre = favoriteGenre;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
