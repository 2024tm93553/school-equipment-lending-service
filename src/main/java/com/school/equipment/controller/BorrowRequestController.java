package com.school.equipment.controller;

import com.school.equipment.dto.borrow.*;
import com.school.equipment.entity.Status;
import com.school.equipment.security.AuthenticationHelper;
import com.school.equipment.service.BorrowRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Borrow Request Management", description = "Equipment borrowing request management endpoints")
public class BorrowRequestController {

    private final BorrowRequestService borrowRequestService;

    public BorrowRequestController(BorrowRequestService borrowRequestService) {
        this.borrowRequestService = borrowRequestService;
    }

    @Operation(
            summary = "Create borrow request",
            description = "Submit a new equipment borrow request. Available for STUDENT and TEACHER roles."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Borrow request created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - validation failed or equipment not available",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - user does not have required role",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Equipment or user not found",
                    content = @Content
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ResponseEntity<CreateResponse> createBorrowRequest(
            @Parameter(description = "Borrow request details", required = true)
            @Valid @RequestBody CreateRequest request,
            Authentication authentication) {
            log.info("Borrow request creation received for equipmentId: {}", request.getEquipmentId());
            Long userId = AuthenticationHelper.getUserIdFromAuthentication(authentication);
            CreateResponse response = borrowRequestService.createBorrowRequest(request, userId);
            return ResponseEntity.ok(response);
        }

    @Operation(
            summary = "Get borrow request by ID",
            description = "Retrieves detailed information about a specific borrow request"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Borrow request retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BorrowRequestResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Borrow request not found",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<BorrowRequestResponse> getRequestById(
            @Parameter(description = "Borrow request ID", required = true, example = "1")
            @PathVariable Long id) {
            log.debug("Fetch request received for requestId: {}", id);
            BorrowRequestResponse response = borrowRequestService.getRequestById(id);
            return ResponseEntity.ok(response);
        }

    @Operation(
            summary = "Get my borrow requests",
            description = "Retrieves all borrow requests submitted by the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User's borrow requests retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = BorrowRequestResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    @GetMapping("/my")
    public ResponseEntity<List<BorrowRequestResponse>> getMyRequests(Authentication authentication) {
            Long userId = AuthenticationHelper.getUserIdFromAuthentication(authentication);
            List<BorrowRequestResponse> response = borrowRequestService.getMyRequests(userId);
            return ResponseEntity.ok(response);
        }

    @Operation(
            summary = "Get pending borrow requests",
            description = "Retrieves all pending borrow requests. Available for ADMIN and LAB_ASSISTANT roles."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pending borrow requests retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = BorrowRequestResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - user does not have required role",
                    content = @Content
            )
    })
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_ASSISTANT')")
    public ResponseEntity<List<BorrowRequestResponse>> getPendingRequests() {
            List<BorrowRequestResponse> response = borrowRequestService.getPendingRequests();
            return ResponseEntity.ok(response);
        }

    @Operation(
            summary = "Get all borrow requests with filters",
            description = "Retrieves all borrow requests with optional filters by status and user ID. Available for ADMIN and LAB_ASSISTANT roles."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Borrow requests retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = BorrowRequestResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status value provided",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - user does not have required role",
                    content = @Content
            )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_ASSISTANT')")
    public ResponseEntity<List<BorrowRequestResponse>> getAllRequests(
            @Parameter(description = "Filter by request status (PENDING, APPROVED, REJECTED, RETURNED)", example = "PENDING")
            @RequestParam(required = false) String status,
            @Parameter(description = "Filter by user ID", example = "1")
            @RequestParam(required = false) Long userId) {
            Status statusEnum = null;
            if (status != null) {
                statusEnum = Status.valueOf(status.toUpperCase());
            }
            List<BorrowRequestResponse> response =
                    borrowRequestService.getRequestsWithFilters(statusEnum, userId);
            return ResponseEntity.ok(response);
        }

    @Operation(
            summary = "Approve borrow request",
            description = "Approve a pending borrow request. Creates booking entries and updates equipment availability. Available for ADMIN and LAB_ASSISTANT roles."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Borrow request approved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BorrowRequestResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid operation - request not pending or equipment not available",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - user does not have required role",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Borrow request or approver not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - equipment no longer available",
                    content = @Content
            )
    })
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_ASSISTANT')")
    public ResponseEntity<BorrowRequestResponse> approveRequest(
            @Parameter(description = "Borrow request ID to approve", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Approval details including approver ID and remarks", required = true)
            @Valid @RequestBody ApproveRequest request) {
            log.info("Approve request received for requestId: {}", id);
            BorrowRequestResponse response = borrowRequestService.approveRequest(id, request);
            return ResponseEntity.ok(response);
        }

    @Operation(
            summary = "Reject borrow request",
            description = "Reject a pending borrow request with remarks. Available for ADMIN and LAB_ASSISTANT roles."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Borrow request rejected successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BorrowRequestResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid operation - only pending requests can be rejected",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - user does not have required role",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Borrow request not found",
                    content = @Content
            )
    })
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_ASSISTANT')")
    public ResponseEntity<BorrowRequestResponse> rejectRequest(
            @Parameter(description = "Borrow request ID to reject", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Rejection details including remarks", required = true)
            @Valid @RequestBody RejectRequest request) {
            log.info("Reject request received for requestId: {}", id);
            BorrowRequestResponse response = borrowRequestService.rejectRequest(id, request);
            return ResponseEntity.ok(response);
        }

    @Operation(
            summary = "Mark equipment as returned",
            description = "Mark an approved borrow request as returned. Updates equipment availability and releases bookings. Available for ADMIN and LAB_ASSISTANT roles."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Equipment marked as returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BorrowRequestResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid operation - only approved requests can be marked as returned",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - user does not have required role",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Borrow request not found",
                    content = @Content
            )
    })
    @PutMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_ASSISTANT')")
    public ResponseEntity<BorrowRequestResponse> markAsReturned(
            @Parameter(description = "Borrow request ID to mark as returned", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Return details including return date and equipment condition", required = true)
            @Valid @RequestBody ReturnRequest request) {
            log.info("Return request received for requestId: {}", id);
            BorrowRequestResponse response = borrowRequestService.markAsReturned(id, request);
            return ResponseEntity.ok(response);
        }
}
