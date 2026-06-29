package com.hrportal.controller;

import com.hrportal.dto.request.ChangePasswordRequest;
import com.hrportal.dto.request.UpdateProfileRequest;
import com.hrportal.dto.response.ApiResponse;
import com.hrportal.dto.response.EmployeeResponse;
import com.hrportal.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class UserController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        EmployeeResponse response = employeeService.getMyProfile(username);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        String username = authentication.getName();
        EmployeeResponse response = employeeService.updateMyProfile(username, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        String username = authentication.getName();
        employeeService.changePassword(username, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
