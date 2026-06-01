package com.lms.leave.service;

import com.lms.leave.config.KafkaConfig;
import com.lms.leave.dto.*;
import com.lms.leave.entity.LeaveRequest;
import com.lms.leave.exception.*;
import com.lms.leave.messaging.*;
import com.lms.leave.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRepo;
    private final EmployeeClientService  employeeClientService; // Circuit-breaker wrapped
    private final LeaveEventPublisher    publisher;

    @Transactional
    public LeaveRequestResponse applyLeave(String employeeId, ApplyLeaveRequest req) {
        UUID empId = UUID.fromString(employeeId);

        if (req.getEndDate().isBefore(req.getStartDate()))
            throw new ValidationException("End date must be >= start date");

        long days = ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1;
        if (days != req.getNumberOfDays())
            throw new ValidationException("numberOfDays mismatch: expected " + days);

        if (!leaveRepo.findOverlapping(empId, req.getStartDate(), req.getEndDate()).isEmpty())
            throw new OverlapException("leave request already exists for the given date range");

        // Circuit-breaker wrapped balance check
        boolean sufficient = employeeClientService.checkBalance(
                empId, req.getLeaveType().toUpperCase(), req.getNumberOfDays());
        if (!sufficient)
            throw new InsufficientBalanceException(
                    "Insufficient " + req.getLeaveType() + " balance for " + req.getNumberOfDays() + " days");

        LeaveRequest leave = LeaveRequest.builder()
                .employeeId(empId).managerId(req.getManagerId())
                .leaveType(req.getLeaveType().toUpperCase())
                .startDate(req.getStartDate()).endDate(req.getEndDate())
                .numberOfDays(req.getNumberOfDays()).reason(req.getReason())
                .status("PENDING").build();
        leave = leaveRepo.save(leave);

        publisher.publish(KafkaConfig.TOPIC_LEAVE_APPLIED, toEvent("LEAVE_APPLIED", leave));
        log.info("[LEAVE] Applied leaveId={} employee={}", leave.getId(), empId);
        return toResponse(leave);
    }

    public Page<LeaveRequestResponse> getMyLeaves(String employeeId, String status, Pageable pageable) {
        UUID empId = UUID.fromString(employeeId);
        Page<LeaveRequest> page = (status != null)
                ? leaveRepo.findByEmployeeIdAndStatus(empId, status, pageable)
                : leaveRepo.findByEmployeeId(empId, pageable);
        return page.map(this::toResponse);
    }

    public Page<LeaveRequestResponse> getTeamLeaves(UUID managerId, String status, Pageable pageable) {
        Page<LeaveRequest> page = (status != null)
                ? leaveRepo.findByManagerIdAndStatus(managerId, status, pageable)
                : leaveRepo.findByManagerId(managerId, pageable);
        return page.map(this::toResponse);
    }

    @Transactional
    public LeaveRequestResponse cancel(UUID leaveId, String employeeId) {
        LeaveRequest leave = findById(leaveId);
        if (!leave.getEmployeeId().toString().equals(employeeId))
            throw new ForbiddenException("You can only cancel your own leave requests");
        if (!"PENDING".equals(leave.getStatus()))
            throw new ValidationException("Only PENDING leaves can be cancelled");
        leave.setStatus("CANCELLED");
        log.info("[LEAVE] Cancelled leaveId={}", leaveId);
        return toResponse(leaveRepo.save(leave));
    }

    @Transactional
    public LeaveRequestResponse approve(UUID leaveId, String managerId, ApproveRejectRequest req) {
        LeaveRequest leave = findById(leaveId);
        if (!leave.getManagerId().toString().equals(managerId))
            throw new ForbiddenException("You can only approve requests assigned to you");
        if (!"PENDING".equals(leave.getStatus()))
            throw new ValidationException("Only PENDING leaves can be approved");

        // Circuit-breaker wrapped deduction
        employeeClientService.deductBalance(
                leave.getEmployeeId(), leave.getLeaveType(), leave.getNumberOfDays());

        leave.setStatus("APPROVED");
        leave.setComments(req != null ? req.getComments() : null);
        leave = leaveRepo.save(leave);

        publisher.publish(KafkaConfig.TOPIC_LEAVE_APPROVED, toEvent("LEAVE_APPROVED", leave));
        log.info("[LEAVE] Approved leaveId={} by manager={}", leaveId, managerId);
        return toResponse(leave);
    }

    @Transactional
    public LeaveRequestResponse reject(UUID leaveId, String managerId, ApproveRejectRequest req) {
        LeaveRequest leave = findById(leaveId);
        if (!leave.getManagerId().toString().equals(managerId))
            throw new ForbiddenException("You can only reject requests assigned to you");
        if (!"PENDING".equals(leave.getStatus()))
            throw new ValidationException("Only PENDING leaves can be rejected");
        if (req == null || req.getRejectionReason() == null || req.getRejectionReason().isBlank())
            throw new ValidationException("Rejection reason is required");

        leave.setStatus("REJECTED");
        leave.setRejectionReason(req.getRejectionReason());
        leave = leaveRepo.save(leave);

        publisher.publish(KafkaConfig.TOPIC_LEAVE_REJECTED, toEvent("LEAVE_REJECTED", leave));
        log.info("[LEAVE] Rejected leaveId={} by manager={}", leaveId, managerId);
        return toResponse(leave);
    }

    private LeaveRequest findById(UUID id) {
        return leaveRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + id));
    }

    private LeaveRequestResponse toResponse(LeaveRequest l) {
        return LeaveRequestResponse.builder()
                .id(l.getId()).employeeId(l.getEmployeeId()).managerId(l.getManagerId())
                .leaveType(l.getLeaveType()).status(l.getStatus()).reason(l.getReason())
                .rejectionReason(l.getRejectionReason()).comments(l.getComments())
                .startDate(l.getStartDate()).endDate(l.getEndDate())
                .numberOfDays(l.getNumberOfDays()).appliedAt(l.getAppliedAt()).updatedAt(l.getUpdatedAt())
                .build();
    }

    private LeaveEvent toEvent(String type, LeaveRequest l) {
        return LeaveEvent.builder().eventType(type)
                .leaveRequestId(l.getId()).employeeId(l.getEmployeeId()).managerId(l.getManagerId())
                .leaveType(l.getLeaveType()).status(l.getStatus()).reason(l.getReason())
                .rejectionReason(l.getRejectionReason()).comments(l.getComments())
                .startDate(l.getStartDate()).endDate(l.getEndDate())
                .numberOfDays(l.getNumberOfDays()).timestamp(LocalDateTime.now()).build();
    }
}
