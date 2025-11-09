package com.school.equipment.service;

import com.school.equipment.dto.borrow.*;
import com.school.equipment.entity.*;
import com.school.equipment.repository.BorrowRequestRepository;
import com.school.equipment.repository.EquipmentBookingRepository;
import com.school.equipment.repository.EquipmentRepository;
import com.school.equipment.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
        // Validate equipment exists
        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
            .orElseThrow(() -> new RuntimeException("Equipment not found"));

        // Validate user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate dates
        if (request.getFromDate().isAfter(request.getToDate())) {
            throw new RuntimeException("From date cannot be after to date");
        }

        if (request.getFromDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("From date cannot be in the past");
        }

        // Check if enough equipment is available for the requested period
        if (!isEquipmentAvailable(request.getEquipmentId(), request.getQuantity(),
                                request.getFromDate(), request.getToDate())) {
            throw new RuntimeException("Not enough equipment available for the requested period");
        }

        // Create borrow request
        BorrowRequest borrowRequest = new BorrowRequest();
        borrowRequest.setEquipment(equipment);
        borrowRequest.setRequestedBy(user);
        borrowRequest.setQuantity(request.getQuantity());
        borrowRequest.setFromDate(request.getFromDate());
        borrowRequest.setToDate(request.getToDate());
        borrowRequest.setReason(request.getReason());
        borrowRequest.setStatus(Status.PENDING);

        BorrowRequest savedRequest = borrowRequestRepository.save(borrowRequest);

        return new CreateResponse(
            savedRequest.getRequestId(),
            savedRequest.getStatus(),
            "Request submitted for approval"
        );
    }

    @Transactional
    public BorrowRequestResponse approveRequest(Long requestId, ApproveRequest approveRequest) {
        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found"));

        if (borrowRequest.getStatus() != Status.PENDING) {
            throw new RuntimeException("Only pending requests can be approved");
        }

        User approvedBy = userRepository.findById(approveRequest.getApprovedBy())
            .orElseThrow(() -> new RuntimeException("Approver not found"));

        // Double-check availability before approval
        if (!isEquipmentAvailable(borrowRequest.getEquipment().getEquipmentId(),
                                borrowRequest.getQuantity(),
                                borrowRequest.getFromDate(),
                                borrowRequest.getToDate())) {
            throw new RuntimeException("Equipment no longer available for the requested period");
        }

        // Update request status
        borrowRequest.setStatus(Status.APPROVED);
        borrowRequest.setApprovedBy(approvedBy);
        borrowRequest.setRemarks(approveRequest.getRemarks());

        // Create booking entries for each date
        createBookingEntries(borrowRequest);

        // Update equipment available quantity
        Equipment equipment = borrowRequest.getEquipment();
        equipment.setAvailableQuantity(equipment.getAvailableQuantity() - borrowRequest.getQuantity());
        equipmentRepository.save(equipment);

        BorrowRequest savedRequest = borrowRequestRepository.save(borrowRequest);
        return mapToResponse(savedRequest);
    }

    @Transactional
    public BorrowRequestResponse rejectRequest(Long requestId, RejectRequest rejectRequest) {
        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found"));

        if (borrowRequest.getStatus() != Status.PENDING) {
            throw new RuntimeException("Only pending requests can be rejected");
        }

        borrowRequest.setStatus(Status.REJECTED);
        borrowRequest.setRemarks(rejectRequest.getRemarks());

        BorrowRequest savedRequest = borrowRequestRepository.save(borrowRequest);
        return mapToResponse(savedRequest);
    }

    @Transactional
    public BorrowRequestResponse markAsReturned(Long requestId, ReturnRequest returnRequest) {
        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found"));

        if (borrowRequest.getStatus() != Status.APPROVED) {
            throw new RuntimeException("Only approved requests can be marked as returned");
        }

        borrowRequest.setStatus(Status.RETURNED);
        borrowRequest.setReturnDate(returnRequest.getReturnDate());
        borrowRequest.setConditionAfterUse(returnRequest.getConditionAfterUse());

        // Release booking entries
        List<EquipmentBooking> bookings = equipmentBookingRepository.findByBorrowRequestRequestId(requestId);
        bookings.forEach(booking -> booking.setStatus(EquipmentBooking.Status.RELEASED));
        equipmentBookingRepository.saveAll(bookings);

        // Update equipment available quantity
        Equipment equipment = borrowRequest.getEquipment();
        equipment.setAvailableQuantity(equipment.getAvailableQuantity() + borrowRequest.getQuantity());
        equipmentRepository.save(equipment);

        BorrowRequest savedRequest = borrowRequestRepository.save(borrowRequest);
        return mapToResponse(savedRequest);
    }

    public BorrowRequestResponse getRequestById(Long requestId) {
        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found"));
        return mapToResponse(borrowRequest);
    }

    public List<BorrowRequestResponse> getMyRequests(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<BorrowRequest> requests = borrowRequestRepository.findByRequestedBy(user);
        return requests.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<BorrowRequestResponse> getPendingRequests() {
        List<BorrowRequest> requests = borrowRequestRepository.findByStatus(Status.PENDING);
        return requests.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<BorrowRequestResponse> getRequestsWithFilters(Status status, Long userId) {
        List<BorrowRequest> requests = borrowRequestRepository.findRequestsWithFilters(status, userId);
        return requests.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private boolean isEquipmentAvailable(Long equipmentId, Integer requestedQuantity, LocalDate fromDate, LocalDate toDate) {
        Equipment equipment = equipmentRepository.findById(equipmentId).orElse(null);
        if (equipment == null) {
            return false;
        }

        // Check each date in the range
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
        LocalDate date = borrowRequest.getFromDate();
        while (!date.isAfter(borrowRequest.getToDate())) {
            EquipmentBooking booking = new EquipmentBooking();
            booking.setBorrowRequest(borrowRequest);
            booking.setEquipment(borrowRequest.getEquipment());
            booking.setBookingDate(date);
            booking.setQuantity(borrowRequest.getQuantity());
            booking.setStatus(EquipmentBooking.Status.ACTIVE);

            equipmentBookingRepository.save(booking);
            date = date.plusDays(1);
        }
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
