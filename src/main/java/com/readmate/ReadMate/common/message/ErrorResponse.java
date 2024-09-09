package com.readmate.ReadMate.common.message;


import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ErrorResponse {
    private int status;
    private String code;
    private String error;
    public ErrorResponse(ErrorCode errorCode){
        this.status = errorCode.getStatus();
        this.code = errorCode.getCode();
        this.error = errorCode.getMessage();
    }
}
