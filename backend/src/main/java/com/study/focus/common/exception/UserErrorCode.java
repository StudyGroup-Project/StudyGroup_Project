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
    FILE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR,"현재 파일과 관련하여 서버에 문제가 있습니다."),
    URL_FORBIDDEN(HttpStatus.FORBIDDEN,"사용자는 해당 기능을 사용할 수 없습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    DUPLICATE_LOGIN_ID(HttpStatus.BAD_REQUEST, "이미 존재하는 아이디입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    USER_ID_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 ID 형식입니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 RefreshToken 입니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Refresh Token이 만료되었습니다."),
    SYSTEM_CREDENTIAL_NOT_FOUND(HttpStatus.NOT_FOUND, "SystemCredential이 존재하지 않습니다."),
    OAUTH_CREDENTIAL_NOT_FOUND(HttpStatus.NOT_FOUND, "OAuthCredential이 존재하지 않습니다."),
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저의 프로필을 찾을 수 없습니다."),
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth Provider입니다.");


    private final HttpStatus httpStatus;
    private final String message;
}
