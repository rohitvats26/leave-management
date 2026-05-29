package com.lms.leave.repository;

import com.lms.leave.entity.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.*;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {
    Page<LeaveRequest> findByEmployeeId(UUID empId, Pageable pageable);

    Page<LeaveRequest> findByEmployeeIdAndStatus(UUID empId, String status, Pageable pageable);

    Page<LeaveRequest> findByManagerId(UUID mgrId, Pageable pageable);

    Page<LeaveRequest> findByManagerIdAndStatus(UUID mgrId, String status, Pageable pageable);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :empId " +
            "AND lr.status IN ('PENDING','APPROVED') " +
            "AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    List<LeaveRequest> findOverlapping(@Param("empId") UUID empId,
                                       @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
