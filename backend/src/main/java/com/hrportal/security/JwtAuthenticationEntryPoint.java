package com.hrportal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrportal.dto.response.ApiResponse;
import com.hrportal.dto.response.ErrorResponse;
import com.hrportal.exception.BusinessException;
import com.hrportal.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.debug("Unauthorized request: {}", authException.getMessage());

        ErrorCode errorCode = ErrorCode.TOKEN_INVALID;
        Exception jwtException = (Exception) request.getAttribute("jwtException");
        if (jwtException instanceof BusinessException be) {
            errorCode = be.getErrorCode();
        }

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .status(errorCode.getHttpStatus().value())
                .build();

        objectMapper.writeValue(response.getWriter(), ApiResponse.error(errorResponse));
    }
}
