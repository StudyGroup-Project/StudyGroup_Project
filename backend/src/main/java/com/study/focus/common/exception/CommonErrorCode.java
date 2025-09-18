package com.study.focus.common.exception;

import com.study.focus.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INVALID_PARAMETER(HttpStatus.BAD_REQUEST,"유효하지 않은 파라미터가 포함"),
    FORMAT_MISMATCH(HttpStatus.BAD_REQUEST,"타입 오류 혹은 JSON 형식 에러 "),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST,"타입 오류 발생 "),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST,"잘못된 요청입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");


    private final HttpStatus httpStatus;
    private final String message;


}