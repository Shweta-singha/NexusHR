package org.Employee.service;
import java.util.Collections;
import org.Employee.entity.Employee;
import org.Employee.repository.EmployeeRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;


@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    public CustomUserDetailsService(
            EmployeeRepository employeeRepository) {

        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Employee employee =
                employeeRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "User not found"));

        return new User(
                employee.getUsername(),
                employee.getPassword(),
                Collections.singleton(
                        new SimpleGrantedAuthority(
                                "ROLE_" + employee.getRole())));
    }
}
