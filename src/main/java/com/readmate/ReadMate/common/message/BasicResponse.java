package com.readmate.ReadMate.common.message;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.readmate.ReadMate.common.dto.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasicResponse<T>{
    private String message;
    private Integer statusCode;
    private T data;
    private PageInfo pageInfo;

    private static final String SUCCESS = "Success";

    public static <T> BasicResponse<T> of(HttpStatus statusCode, String message, T data) {
        return new BasicResponse<>(message, statusCode.value(), data, null);
    }

    public static BasicResponse<Void> ofMessage(String message) {
        return new BasicResponse<>(message, HttpStatus.OK.value(), null, null);
    }


    public static <T> BasicResponse<T> ofSuccess(T data){
        return new BasicResponse<>(SUCCESS, HttpStatus.OK.value(), data, null);
    }

    //페이지네이션
    public static <T> BasicResponse<T> ofSuccess(T data, PageInfo pageInfo) {
        return new BasicResponse<>(SUCCESS, HttpStatus.OK.value(), data, pageInfo);
    }

    public static <T> BasicResponse<T> ofCreateSuccess(T data){
        return new BasicResponse<>(SUCCESS, HttpStatus.CREATED.value(), data, null);
    }

    public static <T> BasicResponse<T> ofError(String message, int statusCode) {
        return new BasicResponse<>(message, statusCode, null, null);
    }

    public static <T> BasicResponse<List<T>> ofSuccessWithPageInfo(List<T> content, PageInfo pageInfo) {
        return new BasicResponse<>(SUCCESS, HttpStatus.OK.value(), content, pageInfo);
    }

}
