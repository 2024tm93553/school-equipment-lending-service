package com.school.equipment.controller;

import com.school.equipment.dto.borrow.*;
import com.school.equipment.entity.Status;
import com.school.equipment.security.AuthenticationHelper;
import com.school.equipment.service.BorrowRequestService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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

    private final BorrowRequestService borrowRequestService;

    public BorrowRequestController(BorrowRequestService borrowRequestService) {
        this.borrowRequestService = borrowRequestService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ResponseEntity<CreateResponse> createBorrowRequest(
            @Valid @RequestBody CreateRequest request,
            Authentication authentication) {
            log.info("Borrow request creation received for equipmentId: {}", request.getEquipmentId());
            Long userId = AuthenticationHelper.getUserIdFromAuthentication(authentication);
            CreateResponse response = borrowRequestService.createBorrowRequest(request, userId);
            return ResponseEntity.ok(response);
        }

    @GetMapping("/{id}")
    public ResponseEntity<BorrowRequestResponse> getRequestById(@PathVariable Long id) {
            log.debug("Fetch request received for requestId: {}", id);
            BorrowRequestResponse response = borrowRequestService.getRequestById(id);
            return ResponseEntity.ok(response);
        }

    @GetMapping("/my")
    public ResponseEntity<List<BorrowRequestResponse>> getMyRequests(Authentication authentication) {
            Long userId = AuthenticationHelper.getUserIdFromAuthentication(authentication);
            List<BorrowRequestResponse> response = borrowRequestService.getMyRequests(userId);
            return ResponseEntity.ok(response);
        }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_ASSISTANT')")
    public ResponseEntity<List<BorrowRequestResponse>> getPendingRequests() {
            List<BorrowRequestResponse> response = borrowRequestService.getPendingRequests();
            return ResponseEntity.ok(response);
        }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_ASSISTANT')")
    public ResponseEntity<List<BorrowRequestResponse>> getAllRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId) {
            Status statusEnum = null;
            if (status != null) {
                statusEnum = Status.valueOf(status.toUpperCase());
            }
            List<BorrowRequestResponse> response =
                    borrowRequestService.getRequestsWithFilters(statusEnum, userId);
            return ResponseEntity.ok(response);
        }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_ASSISTANT')")
    public ResponseEntity<BorrowRequestResponse> approveRequest(
            @PathVariable Long id,
            @Valid @RequestBody ApproveRequest request) {
            log.info("Approve request received for requestId: {}", id);
            BorrowRequestResponse response = borrowRequestService.approveRequest(id, request);
            return ResponseEntity.ok(response);
        }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_ASSISTANT')")
    public ResponseEntity<BorrowRequestResponse> rejectRequest(
            @PathVariable Long id,
            @Valid @RequestBody RejectRequest request) {
            log.info("Reject request received for requestId: {}", id);
            BorrowRequestResponse response = borrowRequestService.rejectRequest(id, request);
            return ResponseEntity.ok(response);
        }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_ASSISTANT')")
    public ResponseEntity<BorrowRequestResponse> markAsReturned(
            @PathVariable Long id,
            @Valid @RequestBody ReturnRequest request) {
            log.info("Return request received for requestId: {}", id);
            BorrowRequestResponse response = borrowRequestService.markAsReturned(id, request);
            return ResponseEntity.ok(response);
        }
    }
