package com.hrportal.service;

import com.hrportal.domain.Employee;
import com.hrportal.exception.BusinessException;
import com.hrportal.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackgroundCheckService {

    private final WebClient backgroundCheckWebClient;

    @Value("${background-check.timeout-seconds}")
    private int timeoutSeconds;

    public Object requestCheck(Employee employee) {
        Map<String, Object> requestBody = Map.of(
                "employeeId", employee.getEmployeeId(),
                "firstName", employee.getFirstName(),
                "lastName", employee.getLastName(),
                "dateOfBirth", employee.getDateOfBirth() != null ? employee.getDateOfBirth().toString() : ""
        );

        return backgroundCheckWebClient.post()
                .uri("/background-checks")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class).map(body ->
                                new BusinessException(ErrorCode.BACKGROUND_CHECK_API_ERROR)))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new BusinessException(ErrorCode.BACKGROUND_CHECK_API_ERROR)))
                .bodyToMono(Object.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .onErrorMap(java.util.concurrent.TimeoutException.class,
                        e -> new BusinessException(ErrorCode.BACKGROUND_CHECK_API_TIMEOUT))
                .onErrorMap(e -> !(e instanceof BusinessException),
                        e -> {
                            log.error("Background check API error: {}", e.getMessage());
                            return new BusinessException(ErrorCode.BACKGROUND_CHECK_API_ERROR);
                        })
                .block();
    }

    public Object getCheckResult(String checkId) {
        return backgroundCheckWebClient.get()
                .uri("/background-checks/{checkId}", checkId)
                .retrieve()
                .onStatus(status -> status.value() == 404, response ->
                        Mono.error(new BusinessException(ErrorCode.BACKGROUND_CHECK_NOT_FOUND)))
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class).map(body ->
                                new BusinessException(ErrorCode.BACKGROUND_CHECK_API_ERROR)))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new BusinessException(ErrorCode.BACKGROUND_CHECK_API_ERROR)))
                .bodyToMono(Object.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .onErrorMap(java.util.concurrent.TimeoutException.class,
                        e -> new BusinessException(ErrorCode.BACKGROUND_CHECK_API_TIMEOUT))
                .onErrorMap(e -> !(e instanceof BusinessException),
                        e -> {
                            log.error("Background check API error: {}", e.getMessage());
                            return new BusinessException(ErrorCode.BACKGROUND_CHECK_API_ERROR);
                        })
                .block();
    }

    public Object listChecksByEmployee(String employeeId) {
        return backgroundCheckWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/background-checks")
                        .queryParam("employeeId", employeeId)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class).map(body ->
                                new BusinessException(ErrorCode.BACKGROUND_CHECK_API_ERROR)))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new BusinessException(ErrorCode.BACKGROUND_CHECK_API_ERROR)))
                .bodyToMono(Object.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .onErrorMap(java.util.concurrent.TimeoutException.class,
                        e -> new BusinessException(ErrorCode.BACKGROUND_CHECK_API_TIMEOUT))
                .onErrorMap(e -> !(e instanceof BusinessException),
                        e -> {
                            log.error("Background check API error: {}", e.getMessage());
                            return new BusinessException(ErrorCode.BACKGROUND_CHECK_API_ERROR);
                        })
                .block();
    }
}
