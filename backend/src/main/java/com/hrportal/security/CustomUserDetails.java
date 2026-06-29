package com.hrportal.security;

import com.hrportal.domain.Employee;
import com.hrportal.domain.enums.EmploymentStatus;
import com.hrportal.domain.enums.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String employeeId;
    private final String username;
    private final String password;
    private final String name;
    private final Role role;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Employee employee) {
        this.id = employee.getId();
        this.employeeId = employee.getEmployeeId();
        this.username = employee.getUsername();
        this.password = employee.getPassword();
        this.name = employee.getFirstName() + " " + employee.getLastName();
        this.role = employee.getRole();
        this.enabled = employee.getStatus() == EmploymentStatus.ACTIVE;
        this.authorities = List.of(new SimpleGrantedAuthority(employee.getRole().name()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
