package com.lms.employee.service;

import com.lms.employee.dto.*;
import com.lms.employee.entity.*;
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

    private final EmployeeRepository employeeRepo;
    private final LeaveBalanceRepository balanceRepo;

    private static final Map<String, Integer> DEFAULT_BALANCES = Map.of(
            "CASUAL", 12, "SICK", 10, "PRIVILEGE", 15);

    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest req) {
        if (employeeRepo.existsByEmail(req.getEmail()))
            throw new IllegalArgumentException("Email already exists: " + req.getEmail());
        if (employeeRepo.existsByUsername(req.getUsername()))
            throw new IllegalArgumentException("Username already exists: " + req.getUsername());

        Employee emp = Employee.builder()
                .firstName(req.getFirstName()).lastName(req.getLastName())
                .email(req.getEmail()).username(req.getUsername())
                .department(req.getDepartment()).managerId(req.getManagerId())
                .role(req.getRole()).enabled(true).build();
        emp = employeeRepo.save(emp);

        // Auto-init leave balances
        final UUID empId = emp.getId();
        DEFAULT_BALANCES.forEach((type, days) ->
                balanceRepo.save(LeaveBalance.builder()
                        .employeeId(empId).leaveType(type)
                        .allocated(days).used(0).build()));

        log.info("Created employee {} with default leave balances", emp.getUsername());
        return toResponse(emp);
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
                .orElseThrow(() -> new ResourceNotFoundException("Balance not found for " + leaveType));
        if (balance.getRemaining() < days)
            throw new IllegalArgumentException("Insufficient " + leaveType + " balance. Available: "
                    + balance.getRemaining() + ", Requested: " + days);
        balance.setUsed(balance.getUsed() + days);
        balanceRepo.save(balance);
        log.info("Deducted {} days of {} for employee {}", days, leaveType, employeeId);
    }

    public boolean hasEnoughBalance(UUID employeeId, String leaveType, int days) {
        return balanceRepo.findByEmployeeIdAndLeaveType(employeeId, leaveType)
                .map(b -> b.getRemaining() >= days).orElse(false);
    }

    private Employee findById(UUID id) {
        return employeeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
    }

    private EmployeeResponse toResponse(Employee e) {
        List<LeaveBalanceDto> balances = balanceRepo.findByEmployeeId(e.getId()).stream()
                .map(b -> LeaveBalanceDto.builder()
                        .leaveType(b.getLeaveType()).allocated(b.getAllocated())
                        .used(b.getUsed()).remaining(b.getRemaining()).build())
                .collect(Collectors.toList());
        return EmployeeResponse.builder()
                .id(e.getId()).firstName(e.getFirstName()).lastName(e.getLastName())
                .email(e.getEmail()).username(e.getUsername()).department(e.getDepartment())
                .managerId(e.getManagerId()).role(e.getRole()).leaveBalances(balances).build();
    }
}
