package com.hrportal.controller;

import com.hrportal.domain.Employee;
import com.hrportal.dto.request.CreateEmployeeRequest;
import com.hrportal.dto.request.UpdateEmployeeRequest;
import com.hrportal.dto.response.ApiResponse;
import com.hrportal.dto.response.EmployeeResponse;
import com.hrportal.dto.response.EmployeeSummaryResponse;
import com.hrportal.service.BackgroundCheckService;
import com.hrportal.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final EmployeeService employeeService;
    private final BackgroundCheckService backgroundCheckService;

    @PostMapping("/employees")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/employees")
    public ResponseEntity<ApiResponse<List<EmployeeSummaryResponse>>> getAllEmployees() {
        List<EmployeeSummaryResponse> response = employeeService.getAllEmployees();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(
            @PathVariable String employeeId) {
        EmployeeResponse response = employeeService.getEmployeeByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/employees/{employeeId}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable String employeeId,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(employeeId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/employees/{employeeId}/terminate")
    public ResponseEntity<ApiResponse<EmployeeResponse>> terminateEmployee(
            @PathVariable String employeeId) {
        EmployeeResponse response = employeeService.terminateEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/employees/{employeeId}/background-checks")
    public ResponseEntity<ApiResponse<Object>> requestBackgroundCheck(
            @PathVariable String employeeId) {
        Employee employee = employeeService.getEmployeeEntityByEmployeeId(employeeId);
        Object result = backgroundCheckService.requestCheck(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    @GetMapping("/employees/{employeeId}/background-checks")
    public ResponseEntity<ApiResponse<Object>> listBackgroundChecks(
            @PathVariable String employeeId) {
        employeeService.getEmployeeEntityByEmployeeId(employeeId); // 존재 확인
        Object result = backgroundCheckService.listChecksByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/employees/{employeeId}/background-checks/{checkId}")
    public ResponseEntity<ApiResponse<Object>> getBackgroundCheck(
            @PathVariable String employeeId,
            @PathVariable String checkId) {
        employeeService.getEmployeeEntityByEmployeeId(employeeId); // 존재 확인
        Object result = backgroundCheckService.getCheckResult(checkId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
