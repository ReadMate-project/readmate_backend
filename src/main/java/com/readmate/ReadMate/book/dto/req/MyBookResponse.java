package com.readmate.ReadMate.book.dto.req;

import com.readmate.ReadMate.login.entity.User;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class MyBookResponse {

    private Long myBookId;

    private Long bookId;

    private User user;
}
