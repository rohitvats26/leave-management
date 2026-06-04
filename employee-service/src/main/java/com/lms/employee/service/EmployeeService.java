package com.lms.employee.service;

import com.lms.employee.client.AuthClient;
import com.lms.employee.client.dto.CreateUserRequest;
import com.lms.employee.dto.*;
import com.lms.employee.entity.*;
import com.lms.employee.exception.ConflictException;
import com.lms.employee.exception.ResourceNotFoundException;
import com.lms.employee.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private static final String DEFAULT_TEMPORARY_PASSWORD = "Password@123";

    private final EmployeeRepository employeeRepo;
    private final LeaveBalanceRepository balanceRepo;
    private final AuthClient authClient;

    private static final Map<String, Integer> DEFAULT_BALANCES = Map.of(
            "CASUAL", 12, "SICK", 10, "PRIVILEGE", 15);

    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest req) {
        if (employeeRepo.existsByEmail(req.getEmail()))
            throw new ConflictException("An employee with this email already exists. Please use a different email address.");
        if (employeeRepo.existsByUsername(req.getUsername()))
            throw new ConflictException("This username is already taken. Please choose a different username.");

        Employee emp = Employee.builder()
                .firstName(req.getFirstName()).lastName(req.getLastName())
                .email(req.getEmail()).username(req.getUsername())
                .department(req.getDepartment()).managerId(req.getManagerId())
                .role(req.getRole()).enabled(true).build();
        Employee savedEmployee = employeeRepo.saveAndFlush(emp);

        try {
            initializeLeaveBalances(savedEmployee.getId());
            provisionLoginUser(savedEmployee, resolvePassword(req));

            log.info("Created employee {} with default leave balances and login user", savedEmployee.getUsername());
            return toResponse(savedEmployee);
        } catch (RuntimeException ex) {
            compensateProvisionedUser(savedEmployee.getUsername());
            throw ex;
        }
    }

    public EmployeeResponse getEmployee(UUID id) {
        return toResponse(findById(id));
    }

    public List<EmployeeResponse> getTeam(UUID managerId) {
        return employeeRepo.findByManagerId(managerId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<LeaveBalanceDto> getBalance(UUID employeeId) {
        findById(employeeId); // existence check
        return balanceRepo.findByEmployeeId(employeeId).stream()
                .map(b -> LeaveBalanceDto.builder()
                        .leaveType(b.getLeaveType()).allocated(b.getAllocated())
                        .used(b.getUsed()).remaining(b.getRemaining()).build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void deductBalance(UUID employeeId, String leaveType, int days) {
        LeaveBalance balance = balanceRepo.findByEmployeeIdAndLeaveType(employeeId, leaveType)
                .orElseThrow(() -> new ResourceNotFoundException("Leave balance record was not found for leave type: " + leaveType));
        if (balance.getRemaining() < days)
            throw new IllegalArgumentException("Insufficient " + leaveType + " leave balance. Available: "
                    + balance.getRemaining() + ", Requested: " + days);
        balance.setUsed(balance.getUsed() + days);
        balanceRepo.save(balance);
        log.info("Deducted {} days of {} for employee {}", days, leaveType, employeeId);
    }

    public boolean hasEnoughBalance(UUID employeeId, String leaveType, int days) {
        return getRemainingBalance(employeeId, leaveType) >= days;
    }

    public int getRemainingBalance(UUID employeeId, String leaveType) {
        return balanceRepo.findByEmployeeIdAndLeaveType(employeeId, leaveType)
                .map(LeaveBalance::getRemaining)
                .orElse(0);
    }

    private Employee findById(UUID id) {
        return employeeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee record not found for id: " + id));
    }

    private EmployeeResponse toResponse(Employee e) {
        List<LeaveBalanceDto> balances = balanceRepo.findByEmployeeId(e.getId()).stream()
                .map(b -> LeaveBalanceDto.builder()
                        .leaveType(b.getLeaveType()).allocated(b.getAllocated())
                        .used(b.getUsed()).remaining(b.getRemaining()).build())
                .collect(Collectors.toList());

        String managerName = Optional.ofNullable(e.getManagerId())
                .flatMap(employeeRepo::findById)
                .map(m -> m.getFirstName() + " " + m.getLastName())
                .orElse(null);

        return EmployeeResponse.builder()
                .id(e.getId()).firstName(e.getFirstName()).lastName(e.getLastName())
                .email(e.getEmail()).username(e.getUsername()).department(e.getDepartment())
                .managerId(e.getManagerId()).managerName(managerName)
                .role(e.getRole()).leaveBalances(balances).build();
    }

    private void initializeLeaveBalances(UUID employeeId) {
        DEFAULT_BALANCES.forEach((type, days) ->
                balanceRepo.save(LeaveBalance.builder()
                        .employeeId(employeeId).leaveType(type)
                        .allocated(days).used(0).build()));
    }

    private void provisionLoginUser(Employee employee, String password) {
        authClient.createUser(CreateUserRequest.builder()
                .id(employee.getId())
                .username(employee.getUsername())
                .email(employee.getEmail())
                .password(password)
                .role(employee.getRole())
                .build());
    }

    private void compensateProvisionedUser(String username) {
        try {
            authClient.deleteUser(username);
        } catch (RuntimeException compensationEx) {
            log.error("Failed to compensate login user creation for username={}", username, compensationEx);
        }
    }

    private String resolvePassword(CreateEmployeeRequest req) {
        return Optional.ofNullable(req.getPassword())
                .filter(password -> !password.isBlank())
                .orElse(DEFAULT_TEMPORARY_PASSWORD);
    }
}
