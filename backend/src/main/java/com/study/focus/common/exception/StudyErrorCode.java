package com.study.focus.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StudyErrorCode implements ErrorCode {

    MEMBER_NOT_FOUND(HttpStatus.FORBIDDEN, "스터디 멤버가 아니거나 권한이 없습니다."),
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 채팅 메시지를 찾을 수 없습니다."),
    STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 스터디 그룹을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}