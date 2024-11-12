package com.readmate.ReadMate.common.genre;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@JsonFormat(shape = JsonFormat.Shape.STRING)
@RequiredArgsConstructor
public enum Genre {

    가정요리뷰티(1230),
    건강취미레저(55890),
    경제경영(170),
    고전(2105),
    과학(987),
    만화(2551),
    사회과학(798),
    문학(1),
    어린이(1108),
    에세이(55889),
    여행(1196),
    역사(74),
    예술대중문화(517),
    외국어(1322),
    유아(13789),
    인문학(656),
    해외소설(31882),
    자기계발(336),
    잡지(2913),
    장르소설(112011),
    종교역학(1237),
    좋은부모(2030),
    청소년(1137),
    컴퓨터모바일(351);

    private final int categoryId;

    public int getCategoryId() {
        return categoryId;
    }
}
