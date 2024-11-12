package com.readmate.ReadMate.common.genre;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.STRING)
@Getter
public enum Genre {

    가정, 요리, 뷰티, 건강, 취미, 레저, 고전, 역사, 과학, 기술, 사회과학, 어린이, 청소년,
    에세이, 자서전, 여행, 예술, 대중문화, 인문학, 철학, 자기계발, 종교, 명상, 컴퓨터, IT,
    의학, 건축, 디자인, 경제경영, 기타, 소설, 시, 희곡


}
