package com.readmate.ReadMate.book.entity;

import com.readmate.ReadMate.login.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public class MyBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long myBookId;
    @NotNull
    @JoinColumn(name = "book_id")
    private Long bookId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


}
