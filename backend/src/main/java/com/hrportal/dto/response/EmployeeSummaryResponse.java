package com.hrportal.dto.response;

import com.hrportal.domain.Employee;
import com.hrportal.domain.enums.EmploymentStatus;
import com.hrportal.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSummaryResponse {

    private String employeeId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String position;
    private EmploymentStatus status;
    private Role role;
    private LocalDate hireDate;

    public static EmployeeSummaryResponse from(Employee employee) {
        return EmployeeSummaryResponse.builder()
                .employeeId(employee.getEmployeeId())
                .username(employee.getUsername())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .status(employee.getStatus())
                .role(employee.getRole())
                .hireDate(employee.getHireDate())
                .build();
    }
}
