package com.hrportal.dto.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 관리자가 직원 정보를 수정할 때 사용. (직원ID·사용자명 제외 전체 수정 가능)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeRequest {

    private String firstName;
    private String lastName;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    private String phone;
    private String department;
    private String position;
    private LocalDate dateOfBirth;
    private LocalDate hireDate;
}
