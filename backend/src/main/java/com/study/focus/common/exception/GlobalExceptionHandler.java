package com.study.focus.common.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

/*
 모든 예외 응답 body에 problemDetail 객체가 들어간다.
 problemDetail의 모든 필드 사용 x
 기본 -> status, message, uri만 사용
 단 yml 파일에 null 필드의 경우 응답에 필드 자체를 생략하도록 설정
 필드 예외 및 vaild 예외의 경우 -> 키(예외 필드) , 값 (필드 예외에 대한 기본 메세지)로 추가
 */


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    //Domain Exception
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleCustomException(BusinessException e, WebRequest request) {
        log.warn("Custom Exception", e);
        ErrorCode errorCode = e.getErrorCode();
        return createResponse(errorCode.getHttpStatus(), errorCode.getMessage(), null, request);
    }

    // 알 수 없는 예외 공통 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleAllException(Exception e, WebRequest request) {
        log.error("Uncaught Exception", e);
        ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        return createResponse(errorCode.getHttpStatus(), errorCode.getMessage(), null, request);
    }



    //body -> DTO 변환 예외(타입 매핑 불가, json 문법 오류 등)
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("MessageNotReadable Exception", e);
        ErrorCode errorCode = CommonErrorCode.FORMAT_MISMATCH;
        ProblemDetail body = createProblemDetail(errorCode.getHttpStatus(), errorCode.getMessage(), null, request);
        return super.handleExceptionInternal(e, body, headers, errorCode.getHttpStatus(), request);
    }
    // 유효성 검증 예외
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("MethodArgumentNotValid Exception", e);
        ErrorCode errorCode = CommonErrorCode.INVALID_PARAMETER;
        Map<String, Object> fieldErrors = e.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (oldValue, newValue) -> oldValue
                ));
        ProblemDetail body = createProblemDetail(errorCode.getHttpStatus(), errorCode.getMessage(), fieldErrors, request);
        return super.handleExceptionInternal(e, body, headers, errorCode.getHttpStatus(), request);
    }

    // 파라미터 타입 예외 (@RequestParam, @PathVariable)
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("TypeMismatch Exception", e);
        ErrorCode errorCode = CommonErrorCode.TYPE_MISMATCH;
        String requiredType = (e.getRequiredType() != null) ? e.getRequiredType().getSimpleName() : "알 수 없음";
        Map<String, Object> properties = Map.of(
                "parameter", e.getPropertyName(),
                "requiredType", requiredType
        );
        ProblemDetail body = createProblemDetail(errorCode.getHttpStatus(), errorCode.getMessage(), properties, request);
        return super.handleExceptionInternal(e, body, headers, errorCode.getHttpStatus(), request);
    }

    private ProblemDetail createProblemDetail(HttpStatusCode status, String detail, Map<String, Object> properties, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        setInstance(problemDetail, request);
        if (properties != null) {
            problemDetail.setProperties(properties);
        }
        return problemDetail;
    }



    //사용자 정의 예외 응답
    private ResponseEntity<ProblemDetail> createResponse(HttpStatusCode status, String detail, Map<String, Object> properties, WebRequest request) {
        ProblemDetail body = createProblemDetail(status, detail, properties, request);
        return ResponseEntity.status(status).body(body);
    }

    // URI 설정 메서드
    private void setInstance(ProblemDetail problemDetail, WebRequest request) {
        String requestURI = ((ServletWebRequest) request).getRequest().getRequestURI();
        problemDetail.setInstance(URI.create(requestURI));
    }
}
