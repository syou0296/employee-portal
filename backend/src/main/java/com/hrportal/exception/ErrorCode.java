package com.hrportal.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "직원을 찾을 수 없습니다."),
    EMPLOYEE_ALREADY_TERMINATED(HttpStatus.BAD_REQUEST, "이미 퇴사 처리된 직원입니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "인증 토큰이 만료되었습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 토큰입니다."),
    ACCOUNT_DISABLED(HttpStatus.UNAUTHORIZED, "비활성화된 계정입니다. 관리자에게 문의하세요."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INVALID_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "현재 비밀번호가 올바르지 않습니다."),
    PASSWORD_SAME_AS_CURRENT(HttpStatus.BAD_REQUEST, "새 비밀번호는 기존 비밀번호와 달라야 합니다."),
    PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "새 비밀번호와 확인 비밀번호가 일치하지 않습니다."),
    RESET_PASSWORD_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "아이디 또는 생년월일이 올바르지 않습니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    BACKGROUND_CHECK_API_ERROR(HttpStatus.BAD_GATEWAY, "Background Check API 오류가 발생했습니다."),
    BACKGROUND_CHECK_API_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "Background Check API 응답 시간을 초과했습니다."),
    BACKGROUND_CHECK_NOT_FOUND(HttpStatus.NOT_FOUND, "Background Check 결과를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
