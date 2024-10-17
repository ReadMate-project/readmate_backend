package com.readmate.ReadMate.book.service;

import com.readmate.ReadMate.login.security.CustomUserDetails;
import com.readmate.ReadMate.book.dto.req.MyBookResponse;
import com.readmate.ReadMate.book.repository.MyBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyBookService {

    private final MyBookRepository myBookRepository;
    public MyBookResponse getMyBooks(CustomUserDetails userDetails) {

        return null;
    }
}
