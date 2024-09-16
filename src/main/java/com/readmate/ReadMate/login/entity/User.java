package com.readmate.ReadMate.login.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "kakao_id")
    private Long kakaoId;  //카카오 유저 고유 ID -> 회원탈퇴 시 필요

    @Column(length = 50)
    private String nickname; //이걸 수정해서 name으로 사용할 수 있도록!, default가 카카오에 설정된 nickname을 name으로 사용

    //카카오 Oauth 로그인만 구rd", nullable = false,현할시 따로 패스워드가 필요없음
////    @Column(name = "passwo length = 255)
//    private String password;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "favorite_genre")
    private FavoriteGenre favoriteGenre; //로그인 할때 내가 설정할 수 있도록 -> 복수개 선택 가능

    @Column(name = "profile_image_url")
    private String profileImageUrl;  // 프로필 이미지 URL

    @Column
    private String content;  //한줄 소개


}