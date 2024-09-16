package com.readmate.ReadMate.login.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasicResponse  <T>{

    private String message;
    private int statusCode;
    private T data;

    public static <T> BasicResponse<T> ofSuccess(T data) {
        return new BasicResponse<>("SUCCESS", HttpStatus.OK.value(), data);
    }


    public static <T> BasicResponse<T> ofFailure(String message, HttpStatus statusCode) {
        return new BasicResponse<>(message, statusCode.value(), null);
    }
}
