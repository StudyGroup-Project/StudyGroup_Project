package com.study.focus.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    INACTIVE_USER(HttpStatus.FORBIDDEN,"User is inactive"),

    FILE_EMPTY(HttpStatus.BAD_REQUEST,"업로드할 파일이 없습니다."),
    FILENAME_MISSING(HttpStatus.BAD_REQUEST,"파일명이 존재하지 않습니다."),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST,"파일의 허용 용량을 초과했습니다"),
    FILE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR,"파일 업로드에 실패했습니다"),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST,"잘못된 파일 타입을 요청했습니다."),
    FILE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR,"현재 파일과 관련하여 서버에 문제가 있습니다.");



    private  final HttpStatus httpStatus;
    private final String message;
}
