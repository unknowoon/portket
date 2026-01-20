package com.portket.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 일반 에러 (10000번대)
    NOT_FOUND(HttpStatus.NOT_FOUND, "10000", "조회 결과가 없습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "10001", "올바르지 않은 입력값 형식입니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "10002", "올바르지 않은 입력값 형식입니다."),
    NOT_NULL(HttpStatus.BAD_REQUEST, "10003", "필수 입력값이 없습니다."),
    
    // 사용자 관련 에러 (20000번대)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "20000", "해당하는 회원을 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "20001", "비밀번호가 잘못되었습니다."),
    USERNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "20002", "이미 사용 중인 사용자 이메일입니다."),
    
    // 서버 에러 (50000번대)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "50000", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String customMessage;
}
