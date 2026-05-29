package com.lms.employee.repository;

import com.lms.employee.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, UUID> {

    List<LeaveBalance> findByEmployeeId(UUID employeeId);

    Optional<LeaveBalance> findByEmployeeIdAndLeaveType(UUID employeeId, String leaveType);
}
