package com.lms.employee.dto;
import lombok.*;
import java.util.*;
@Data @Builder
public class EmployeeResponse {
    private UUID id;
    private String firstName, lastName, email, username, department, role;
    private UUID managerId;
    private String managerName;
    private List<LeaveBalanceDto> leaveBalances;
}
