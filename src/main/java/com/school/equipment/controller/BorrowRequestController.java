package com.school.equipment.controller;

import com.school.equipment.dto.borrow.*;
import com.school.equipment.entity.Status;
import com.school.equipment.security.AuthenticationHelper;
import com.school.equipment.service.BorrowRequestService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "*")
public class BorrowRequestController {

    @Autowired
    private BorrowRequestService borrowRequestService;

    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT')")
    public ResponseEntity<CreateResponse> createBorrowRequest(
            @Valid @RequestBody CreateRequest request,
            Authentication authentication) {
        try {
            log.info("Borrow request creation received for equipmentId: {}", request.getEquipmentId());
            Long userId = AuthenticationHelper.getUserIdFromAuthentication(authentication);
            CreateResponse response = borrowRequestService.createBorrowRequest(request, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Failed to create borrow request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BorrowRequestResponse> getRequestById(@PathVariable Long id) {
        try {
            log.debug("Fetch request received for requestId: {}", id);
            BorrowRequestResponse response = borrowRequestService.getRequestById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Failed to fetch borrow request {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<BorrowRequestResponse>> getMyRequests(Authentication authentication) {
        try {
            Long userId = AuthenticationHelper.getUserIdFromAuthentication(authentication);
            List<BorrowRequestResponse> response = borrowRequestService.getMyRequests(userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<BorrowRequestResponse>> getPendingRequests() {
        try {
            List<BorrowRequestResponse> response = borrowRequestService.getPendingRequests();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<BorrowRequestResponse>> getAllRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId) {
        try {
            Status statusEnum = null;
            if (status != null) {
                statusEnum = Status.valueOf(status.toUpperCase());
            }
            List<BorrowRequestResponse> response =
                    borrowRequestService.getRequestsWithFilters(statusEnum, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<BorrowRequestResponse> approveRequest(
            @PathVariable Long id,
            @Valid @RequestBody ApproveRequest request) {
        try {
            log.info("Approve request received for requestId: {}", id);
            BorrowRequestResponse response = borrowRequestService.approveRequest(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Failed to approve request {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<BorrowRequestResponse> rejectRequest(
            @PathVariable Long id,
            @Valid @RequestBody RejectRequest request) {
        try {
            log.info("Reject request received for requestId: {}", id);
            BorrowRequestResponse response = borrowRequestService.rejectRequest(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Failed to reject request {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<BorrowRequestResponse> markAsReturned(
            @PathVariable Long id,
            @Valid @RequestBody ReturnRequest request) {
        try {
            log.info("Return request received for requestId: {}", id);
            BorrowRequestResponse response = borrowRequestService.markAsReturned(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Failed to mark request {} as returned: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
