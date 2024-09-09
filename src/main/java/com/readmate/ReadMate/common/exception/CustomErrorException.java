package com.readmate.ReadMate.common.exception;


import com.readmate.ReadMate.common.message.ErrorResponse;
import lombok.Getter;

@Getter
public class CustomErrorException extends RuntimeException{
    private ErrorResponse errorResponse;
    public CustomErrorException(ErrorResponse errorResponse){
        this.errorResponse = errorResponse;
    }
}
