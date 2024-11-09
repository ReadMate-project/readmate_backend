package com.readmate.ReadMate.bookclub.club.dto.req;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BookInfoRequest {

    private Long isbn;
    private LocalDate readingStartDate;
    private LocalDate readingEndDate;
}
