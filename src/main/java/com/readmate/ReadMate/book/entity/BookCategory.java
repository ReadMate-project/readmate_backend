package com.readmate.ReadMate.book.entity;

import lombok.Getter;

@Getter
public enum BookCategory {
    FAMILY_COOKING_BEAUTY(1230, "가정/요리"),
    HEALTH_HOBBY(55890, "건강/취미"),
    ECONOMY(170, "경제"),
    CLASSIC(2105, "고전"),
    SCIENCE(987, "과학"),
    SOCIAL_SCIENCE(798, "사회과학"),
    NOVEL(1, "소설"),
    SF_NOVEL(50930, "SF소설"),
    ROMANCE(50935, "로맨스소설"),
    WORLD_LITERATURE(50955, "세계문학"),
    WORLD_NOVEL(50925, "세계소설"),
    POETRY(50940, "시"),
    SHINCHUN(50942, "신춘문예"),
    ACTION_THRILLER(50933, "액션/스릴러"),
    WOMEN_LITERATURE(51252, "여성문학"),
    HISTORY_NOVEL(50929, "역사소설"),
    ENGLISH_NOVEL(50919, "영미소설"),
    CHINESE_NOVEL(50923, "중국소설"),
    MYSTERY_NOVEL(50926, "추리/미스터리"),
    FANTASY_NOVEL(50928, "판타지소설"),
    KOREAN_NOVEL(50917, "한국소설"),
    HORROR_NOVEL(50931, "호러소설"),
    DRAMA(50948, "희곡"),
    CHILDREN(1108, "어린이"),
    ESSAY(55889, "에세이"),
    TRAVEL(1196, "여행"),
    HISTORY(74, "역사"),
    ART_CULTURE(517, "예술/문화"),
    FOREIGN_LANGUAGE(1322, "외국어"),
    INFANT(13789, "유아"),
    HUMANITIES(656, "인문학"),
    EASTERN_PHILOSOPHY(51393, "동양철학"),
    CULTURE_THEORY(51417, "문화이론"),
    HUMANIST(65237, "인문사상"),
    WESTERN_PHILOSOPHY(51390, "서양철학"),
    RELIGION_MYTHOLOGY(51399, "종교/신화"),
    PSYCHOLOGY(51395, "심리학"),
    ANTHROPOLOGY(51415, "인류학"),
    HUMAN_CRITICISM(51384, "인문비평"),
    HUMAN_ESSAY(51381, "인문에세이"),
    HUMAN_DICTIONARY(51385, "인문사전"),
    READING_WRITING(51403, "책읽기/글쓰기"),
    GENERAL_PHILOSOPHY(51387, "철학일반"),
    SELF_DEVELOPMENT(336, "자기계발"),
    RELIGION_SCIENCE(1237, "종교/역학"),
    GOOD_PARENT(2030, "좋은부모"),
    TEENAGER(1137, "청소년"),
    COMPUTER_MOBILE(351, "컴퓨터/모바일");


    private final int id;
    private final String genreName;

    // Enum 생성자
    BookCategory(int id, String genreName) {
        this.id = id;
        this.genreName = genreName;
    }

    // id 값을 가져오는 메서드
    public int getId() {
        return id;
    }

    // displayName 값을 가져오는 메서드
    public String getGenreName() {
        return genreName;
    }
}
