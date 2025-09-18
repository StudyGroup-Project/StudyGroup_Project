package com.study.focus.common.exception;

import lombok.Getter;

//도메인 예외(기본 예외가 아닌 도메인 에러에 대한 예외 처리)
@Getter
public class BusinessException extends RuntimeException{
    private final  ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, Throwable t){
        super(errorCode.getMessage(),t);
        this.errorCode = errorCode;
    }

}
