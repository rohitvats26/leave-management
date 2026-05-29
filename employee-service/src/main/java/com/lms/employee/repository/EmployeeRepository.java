package com.lms.employee.repository;

import com.lms.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    List<Employee> findByManagerId(UUID managerId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
