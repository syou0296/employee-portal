package com.hrportal.config;

import com.hrportal.domain.Employee;
import com.hrportal.domain.enums.EmploymentStatus;
import com.hrportal.domain.enums.Role;
import com.hrportal.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (employeeRepository.count() == 0) {
            Employee admin = Employee.builder()
                    .employeeId("EMP-2024-000")
                    .username("admin")
                    .password(passwordEncoder.encode("admin1234"))
                    .firstName("Admin")
                    .lastName("User")
                    .email("admin@hrportal.com")
                    .hireDate(LocalDate.of(2020, 1, 1))
                    .role(Role.ROLE_ADMIN)
                    .status(EmploymentStatus.ACTIVE)
                    .build();

            Employee user = Employee.builder()
                    .employeeId("EMP-2024-001")
                    .username("john.doe")
                    .password(passwordEncoder.encode("password123"))
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@hrportal.com")
                    .department("Engineering")
                    .position("Senior Engineer")
                    .hireDate(LocalDate.of(2024, 1, 15))
                    .dateOfBirth(LocalDate.of(1990, 3, 15))
                    .role(Role.ROLE_USER)
                    .status(EmploymentStatus.ACTIVE)
                    .build();

            employeeRepository.save(admin);
            employeeRepository.save(user);
            log.info("DataInitializer: default accounts created (admin, john.doe)");
        }
    }
}
