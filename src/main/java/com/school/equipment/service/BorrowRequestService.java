package com.school.equipment.service;

import com.school.equipment.dto.borrow.*;
import com.school.equipment.entity.*;
import com.school.equipment.repository.BorrowRequestRepository;
import com.school.equipment.repository.EquipmentBookingRepository;
import com.school.equipment.repository.EquipmentRepository;
import com.school.equipment.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BorrowRequestService {

    @Autowired
    private BorrowRequestRepository borrowRequestRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EquipmentBookingRepository equipmentBookingRepository;

    @Transactional
    public CreateResponse createBorrowRequest(CreateRequest request, Long userId) {
        log.info("Creating borrow request - equipmentId: {}, userId: {}, quantity: {}, fromDate: {}, toDate: {}",
                request.getEquipmentId(), userId, request.getQuantity(), request.getFromDate(), request.getToDate());
        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
            .orElseThrow(() -> {
                log.error("Failed to create borrow request - equipment not found: {}", request.getEquipmentId());
                return new RuntimeException("Equipment not found");
            });

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("Failed to create borrow request - user not found: {}", userId);
                return new RuntimeException("User not found");
            });

        if (request.getFromDate().isAfter(request.getToDate())) {
            log.warn("Borrow request validation failed - from date after to date: {} > {}",
                    request.getFromDate(), request.getToDate());
            throw new RuntimeException("From date cannot be after to date");
        }

        if (request.getFromDate().isBefore(LocalDate.now())) {
            log.warn("Borrow request validation failed - from date in the past: {}", request.getFromDate());
            throw new RuntimeException("From date cannot be in the past");
        }

        if (!isEquipmentAvailable(request.getEquipmentId(), request.getQuantity(),
                                request.getFromDate(), request.getToDate())) {
            log.warn("Borrow request failed - equipment not available for requested period. Equipment: {}, Quantity: {}",
                    request.getEquipmentId(), request.getQuantity());
            throw new RuntimeException("Not enough equipment available for the requested period");
        }

        BorrowRequest borrowRequest = new BorrowRequest();
        borrowRequest.setEquipment(equipment);
        borrowRequest.setRequestedBy(user);
        borrowRequest.setQuantity(request.getQuantity());
        borrowRequest.setFromDate(request.getFromDate());
        borrowRequest.setToDate(request.getToDate());
        borrowRequest.setReason(request.getReason());
        borrowRequest.setStatus(Status.PENDING);

        BorrowRequest savedRequest = borrowRequestRepository.save(borrowRequest);
        log.info("Borrow request created successfully - requestId: {}, status: {}",
                savedRequest.getRequestId(), savedRequest.getStatus());

        return new CreateResponse(
            savedRequest.getRequestId(),
            savedRequest.getStatus(),
            "Request submitted for approval"
        );
    }

    @Transactional
    public BorrowRequestResponse approveRequest(Long requestId, ApproveRequest approveRequest) {
        log.info("Approving borrow request - requestId: {}, approvedBy: {}",
                requestId, approveRequest.getApprovedBy());

        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
            .orElseThrow(() -> {
                log.error("Failed to approve request - request not found: {}", requestId);
                return new RuntimeException("Request not found");
            });

        if (borrowRequest.getStatus() != Status.PENDING) {
            log.warn("Failed to approve request - request not in PENDING status: requestId={}, currentStatus={}",
                    requestId, borrowRequest.getStatus());
            throw new RuntimeException("Only pending requests can be approved");
        }

        User approvedBy = userRepository.findById(approveRequest.getApprovedBy())
            .orElseThrow(() -> {
                log.error("Failed to approve request - approver not found: {}", approveRequest.getApprovedBy());
                return new RuntimeException("Approver not found");
            });

        if (!isEquipmentAvailable(borrowRequest.getEquipment().getEquipmentId(),
                                borrowRequest.getQuantity(),
                                borrowRequest.getFromDate(),
                                borrowRequest.getToDate())) {
            log.warn("Failed to approve request - equipment no longer available: requestId={}", requestId);
            throw new RuntimeException("Equipment no longer available for the requested period");
        }

        borrowRequest.setStatus(Status.APPROVED);
        borrowRequest.setApprovedBy(approvedBy);
        borrowRequest.setRemarks(approveRequest.getRemarks());

        createBookingEntries(borrowRequest);
        Equipment equipment = borrowRequest.getEquipment();
        equipment.setAvailableQuantity(equipment.getAvailableQuantity() - borrowRequest.getQuantity());
        equipmentRepository.save(equipment);

        BorrowRequest savedRequest = borrowRequestRepository.save(borrowRequest);
        log.info("Borrow request approved successfully - requestId: {}, equipmentId: {}, quantity: {}",
                savedRequest.getRequestId(), savedRequest.getEquipment().getEquipmentId(),
                savedRequest.getQuantity());
        return mapToResponse(savedRequest);
    }

    @Transactional
    public BorrowRequestResponse rejectRequest(Long requestId, RejectRequest rejectRequest) {
        log.info("Rejecting borrow request - requestId: {}, remarks: {}", requestId, rejectRequest.getRemarks());

        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
            .orElseThrow(() -> {
                log.error("Failed to reject request - request not found: {}", requestId);
                return new RuntimeException("Request not found");
            });

        if (borrowRequest.getStatus() != Status.PENDING) {
            log.warn("Failed to reject request - request not in PENDING status: requestId={}, currentStatus={}",
                    requestId, borrowRequest.getStatus());
            throw new RuntimeException("Only pending requests can be rejected");
        }

        borrowRequest.setStatus(Status.REJECTED);
        borrowRequest.setRemarks(rejectRequest.getRemarks());

        BorrowRequest savedRequest = borrowRequestRepository.save(borrowRequest);
        log.info("Borrow request rejected successfully - requestId: {}", savedRequest.getRequestId());
        return mapToResponse(savedRequest);
    }

    @Transactional
    public BorrowRequestResponse markAsReturned(Long requestId, ReturnRequest returnRequest) {
        log.info("Marking borrow request as returned - requestId: {}, returnDate: {}",
                requestId, returnRequest.getReturnDate());

        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
            .orElseThrow(() -> {
                log.error("Failed to mark as returned - request not found: {}", requestId);
                return new RuntimeException("Request not found");
            });

        if (borrowRequest.getStatus() != Status.APPROVED) {
            log.warn("Failed to mark as returned - request not in APPROVED status: requestId={}, currentStatus={}",
                    requestId, borrowRequest.getStatus());
            throw new RuntimeException("Only approved requests can be marked as returned");
        }

        borrowRequest.setStatus(Status.RETURNED);
        borrowRequest.setReturnDate(returnRequest.getReturnDate());
        borrowRequest.setConditionAfterUse(returnRequest.getConditionAfterUse());

        List<EquipmentBooking> bookings = equipmentBookingRepository.findByBorrowRequestRequestId(requestId);
        bookings.forEach(booking -> booking.setStatus(EquipmentBooking.Status.RELEASED));
        equipmentBookingRepository.saveAll(bookings);

        Equipment equipment = borrowRequest.getEquipment();
        equipment.setAvailableQuantity(equipment.getAvailableQuantity() + borrowRequest.getQuantity());
        equipmentRepository.save(equipment);

        BorrowRequest savedRequest = borrowRequestRepository.save(borrowRequest);
        log.info("Borrow request marked as returned successfully - requestId: {}, equipmentId: {}, quantity: {}, condition: {}",
                savedRequest.getRequestId(), savedRequest.getEquipment().getEquipmentId(),
                savedRequest.getQuantity(), savedRequest.getConditionAfterUse());
        return mapToResponse(savedRequest);
    }

    public BorrowRequestResponse getRequestById(Long requestId) {
        log.debug("Fetching borrow request by id: {}", requestId);
        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found"));
        return mapToResponse(borrowRequest);
    }

    public List<BorrowRequestResponse> getMyRequests(Long userId) {
        log.debug("Fetching borrow requests for user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("Failed to fetch user requests - user not found: {}", userId);
                return new RuntimeException("User not found");
            });

        List<BorrowRequest> requests = borrowRequestRepository.findByRequestedBy(user);
        log.info("Retrieved {} borrow requests for user: {}", requests.size(), userId);
        return requests.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<BorrowRequestResponse> getPendingRequests() {
        log.debug("Fetching all pending borrow requests");
        List<BorrowRequest> requests = borrowRequestRepository.findByStatus(Status.PENDING);
        log.info("Retrieved {} pending borrow requests", requests.size());
        return requests.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<BorrowRequestResponse> getRequestsWithFilters(Status status, Long userId) {
        log.debug("Fetching borrow requests with filters - status: {}, userId: {}", status, userId);
        List<BorrowRequest> requests = borrowRequestRepository.findRequestsWithFilters(status, userId);
        log.info("Retrieved {} borrow requests with filters", requests.size());
        return requests.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private boolean isEquipmentAvailable(Long equipmentId, Integer requestedQuantity, LocalDate fromDate, LocalDate toDate) {
        Equipment equipment = equipmentRepository.findById(equipmentId).orElse(null);
        if (equipment == null) {
            return false;
        }

        LocalDate date = fromDate;
        while (!date.isAfter(toDate)) {
            Integer bookedQuantity = equipmentBookingRepository.getTotalBookedQuantityForDate(equipmentId, date);
            if (bookedQuantity == null) {
                bookedQuantity = 0;
            }

            int availableForDate = equipment.getTotalQuantity() - bookedQuantity;
            if (availableForDate < requestedQuantity) {
                return false;
            }

            date = date.plusDays(1);
        }

        return true;
    }

    private void createBookingEntries(BorrowRequest borrowRequest) {
        log.debug("Creating booking entries for request: {}", borrowRequest.getRequestId());
        int bookingCount = 0;
        LocalDate date = borrowRequest.getFromDate();
        while (!date.isAfter(borrowRequest.getToDate())) {
            EquipmentBooking booking = new EquipmentBooking();
            booking.setBorrowRequest(borrowRequest);
            booking.setEquipment(borrowRequest.getEquipment());
            booking.setBookingDate(date);
            booking.setQuantity(borrowRequest.getQuantity());
            booking.setStatus(EquipmentBooking.Status.ACTIVE);

            equipmentBookingRepository.save(booking);
            bookingCount++;
            date = date.plusDays(1);
        }
        log.info("Created {} booking entries for requestId: {}", bookingCount, borrowRequest.getRequestId());
    }

    private BorrowRequestResponse mapToResponse(BorrowRequest borrowRequest) {
        return new BorrowRequestResponse(
            borrowRequest.getRequestId(),
            borrowRequest.getEquipment().getEquipmentId(),
            borrowRequest.getEquipment().getName(),
            borrowRequest.getRequestedBy().getUserId(),
            borrowRequest.getRequestedBy().getFullName(),
            borrowRequest.getQuantity(),
            borrowRequest.getFromDate(),
            borrowRequest.getToDate(),
            borrowRequest.getReturnDate(),
            borrowRequest.getReason(),
            borrowRequest.getStatus(),
            borrowRequest.getRemarks(),
            borrowRequest.getConditionAfterUse(),
            borrowRequest.getApprovedBy() != null ? borrowRequest.getApprovedBy().getFullName() : null,
            borrowRequest.getCreatedAt() != null ? borrowRequest.getCreatedAt().toString() : null,
            borrowRequest.getUpdatedAt() != null ? borrowRequest.getUpdatedAt().toString() : null
        );
    }
}
