package com.lms.employee.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "leave_balances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private UUID employeeId;
    @Column(nullable = false)
    private String leaveType; // CASUAL, SICK, PRIVILEGE
    @Column(nullable = false)
    private int allocated;
    @Column(nullable = false)
    private int used;

    public int getRemaining() {
        return allocated - used;
    }
}
