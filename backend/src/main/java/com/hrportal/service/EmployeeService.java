package com.hrportal.service;

import com.hrportal.domain.Employee;
import com.hrportal.domain.enums.EmploymentStatus;
import com.hrportal.dto.request.ChangePasswordRequest;
import com.hrportal.dto.request.CreateEmployeeRequest;
import com.hrportal.dto.request.ForgotPasswordRequest;
import com.hrportal.dto.request.UpdateEmployeeRequest;
import com.hrportal.dto.request.UpdateProfileRequest;
import com.hrportal.dto.response.EmployeeResponse;
import com.hrportal.dto.response.EmployeeSummaryResponse;
import com.hrportal.dto.response.ForgotPasswordResponse;
import com.hrportal.exception.BusinessException;
import com.hrportal.exception.ErrorCode;
import com.hrportal.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    // 직원 생성
    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        if (employeeRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        String employeeId = generateEmployeeId();
        Employee employee = Employee.builder()
                .employeeId(employeeId)
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .department(request.getDepartment())
                .position(request.getPosition())
                .dateOfBirth(request.getDateOfBirth())
                .hireDate(request.getHireDate())
                .role(request.getRole())
                .status(EmploymentStatus.ACTIVE)
                .build();
        Employee saved = employeeRepository.save(employee);
        log.debug("Created employee: {}", saved.getEmployeeId());
        return EmployeeResponse.from(saved);
    }

    // 본인 프로필 조회
    @Transactional(readOnly = true)
    public EmployeeResponse getMyProfile(String username) {
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));
        return EmployeeResponse.from(employee);
    }

    // 본인 프로필 수정 (전화번호만)
    @Transactional
    public EmployeeResponse updateMyProfile(String username, UpdateProfileRequest request) {
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));
        if (request.getPhone() != null) {
            employee.setPhone(request.getPhone());
        }
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), employee.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }
        if (passwordEncoder.matches(request.getNewPassword(), employee.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_SAME_AS_CURRENT);
        }
        employee.setPassword(passwordEncoder.encode(request.getNewPassword()));
        employeeRepository.save(employee);
        log.debug("Password changed for user: {}", username);
    }

    // 관리자: 직원 정보 수정 (직원ID·사용자명 제외 전체)
    @Transactional
    public EmployeeResponse updateEmployee(String employeeId, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));
        if (request.getEmail() != null && !request.getEmail().equals(employee.getEmail())) {
            if (employeeRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
            }
            employee.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null) employee.setFirstName(request.getFirstName());
        if (request.getLastName() != null)  employee.setLastName(request.getLastName());
        if (request.getPhone() != null)     employee.setPhone(request.getPhone());
        if (request.getDepartment() != null) employee.setDepartment(request.getDepartment());
        if (request.getPosition() != null)  employee.setPosition(request.getPosition());
        if (request.getDateOfBirth() != null) employee.setDateOfBirth(request.getDateOfBirth());
        if (request.getHireDate() != null)  employee.setHireDate(request.getHireDate());
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    // 관리자: 전체 직원 목록
    @Transactional(readOnly = true)
    public List<EmployeeSummaryResponse> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(EmployeeSummaryResponse::from)
                .collect(Collectors.toList());
    }

    // 관리자: 직원 상세 조회
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByEmployeeId(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));
        return EmployeeResponse.from(employee);
    }

    // 관리자: 퇴사 처리
    @Transactional
    public EmployeeResponse terminateEmployee(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));
        if (employee.getStatus() == EmploymentStatus.TERMINATED) {
            throw new BusinessException(ErrorCode.EMPLOYEE_ALREADY_TERMINATED);
        }
        employee.setStatus(EmploymentStatus.TERMINATED);
        employee.setTerminateDate(LocalDate.now());
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    // 내부: Employee 엔티티 직접 조회
    @Transactional(readOnly = true)
    public Employee getEmployeeEntityByEmployeeId(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    // 비밀번호 찾기: 아이디 + 생년월일 검증 후 임시 비밀번호 발급
    @Transactional
    public ForgotPasswordResponse resetPassword(ForgotPasswordRequest request) {
        Employee employee = employeeRepository.findByUsername(request.getUsername())
                .filter(e -> request.getDateOfBirth() != null
                        && request.getDateOfBirth().equals(e.getDateOfBirth()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RESET_PASSWORD_VERIFICATION_FAILED));
        String tempPassword = generateTemporaryPassword();
        employee.setPassword(passwordEncoder.encode(tempPassword));
        employeeRepository.save(employee);
        log.info("Temporary password issued for user: {}", request.getUsername());
        return ForgotPasswordResponse.builder()
                .temporaryPassword(tempPassword)
                .message("임시 비밀번호가 발급되었습니다. 로그인 후 반드시 비밀번호를 변경해주세요.")
                .build();
    }

    // 임시 비밀번호 생성 (영문 대소문자 + 숫자 + 특수문자 각 1자 이상, 10자)
    private String generateTemporaryPassword() {
        String upper   = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower   = "abcdefghijklmnopqrstuvwxyz";
        String digits  = "0123456789";
        String special = "!@#$%^&*";
        String all     = upper + lower + digits + special;
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        sb.append(upper.charAt(random.nextInt(upper.length())));
        sb.append(lower.charAt(random.nextInt(lower.length())));
        sb.append(digits.charAt(random.nextInt(digits.length())));
        sb.append(special.charAt(random.nextInt(special.length())));
        for (int i = 0; i < 6; i++) {
            sb.append(all.charAt(random.nextInt(all.length())));
        }
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i]; chars[i] = chars[j]; chars[j] = tmp;
        }
        return new String(chars);
    }

    // employeeId 자동 채번 (EMP-{년도}-{3자리 시퀀스})
    private String generateEmployeeId() {
        int year = Year.now().getValue();
        String prefix = "EMP-" + year + "-";
        long count = employeeRepository.countByEmployeeIdStartingWith(prefix);
        return String.format("%s%03d", prefix, count + 1);
    }
}
