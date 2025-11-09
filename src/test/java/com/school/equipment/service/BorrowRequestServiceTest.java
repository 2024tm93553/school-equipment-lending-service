package com.school.equipment.service;

import com.school.equipment.dto.borrow.*;
import com.school.equipment.entity.*;
import com.school.equipment.exception.EquipmentNotAvailableException;
import com.school.equipment.exception.InvalidOperationException;
import com.school.equipment.exception.InvalidRequestException;
import com.school.equipment.exception.ResourceNotFoundException;
import com.school.equipment.repository.BorrowRequestRepository;
import com.school.equipment.repository.EquipmentBookingRepository;
import com.school.equipment.repository.EquipmentRepository;
import com.school.equipment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowRequestServiceTest {

    @Mock
    private BorrowRequestRepository borrowRequestRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EquipmentBookingRepository equipmentBookingRepository;

    @InjectMocks
    private BorrowRequestService borrowRequestService;

    private Equipment testEquipment;
    private User testUser;
    private User testApprover;
    private BorrowRequest testBorrowRequest;
    private CreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setFullName("John Doe");

        testApprover = new User();
        testApprover.setUserId(2L);
        testApprover.setFullName("Admin User");

        testEquipment = new Equipment();
        testEquipment.setEquipmentId(1L);
        testEquipment.setName("Dell Laptop");
        testEquipment.setTotalQuantity(10);
        testEquipment.setAvailableQuantity(8);

        testBorrowRequest = new BorrowRequest();
        testBorrowRequest.setRequestId(1L);
        testBorrowRequest.setEquipment(testEquipment);
        testBorrowRequest.setRequestedBy(testUser);
        testBorrowRequest.setQuantity(2);
        testBorrowRequest.setFromDate(LocalDate.of(2024, 1, 15));
        testBorrowRequest.setToDate(LocalDate.of(2024, 1, 20));
        testBorrowRequest.setReason("Need for project work");
        testBorrowRequest.setStatus(Status.PENDING);
        testBorrowRequest.setCreatedAt(LocalDateTime.now());
        testBorrowRequest.setUpdatedAt(LocalDateTime.now());

        createRequest = new CreateRequest(
                1L,
                2,
                LocalDate.of(2024, 1, 15),
                LocalDate.of(2024, 1, 20),
                "Need for project work"
        );
    }

    @Test
    void createBorrowRequest_Success() {
        // Given
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(equipmentBookingRepository.getTotalBookedQuantityForDate(any(), any())).thenReturn(0);
        when(borrowRequestRepository.save(any(BorrowRequest.class))).thenReturn(testBorrowRequest);

        // When
        CreateResponse response = borrowRequestService.createBorrowRequest(createRequest, 1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getRequestId());
        assertEquals(Status.PENDING, response.getStatus());
        assertEquals("Request submitted for approval", response.getMessage());

        verify(equipmentRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(borrowRequestRepository).save(any(BorrowRequest.class));
    }

    @Test
    void createBorrowRequest_EquipmentNotFound_ThrowsException() {
        // Given
        when(equipmentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            borrowRequestService.createBorrowRequest(createRequest, 1L);
        });

        assertEquals("Equipment not found with id: 1", exception.getMessage());
        verify(equipmentRepository).findById(1L);
        verify(userRepository, never()).findById(any());
        verify(borrowRequestRepository, never()).save(any());
    }

    @Test
    void createBorrowRequest_UserNotFound_ThrowsException() {
        // Given
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            borrowRequestService.createBorrowRequest(createRequest, 1L);
        });

        assertEquals("User not found with id: 1", exception.getMessage());
        verify(equipmentRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(borrowRequestRepository, never()).save(any());
    }

    @Test
    void createBorrowRequest_FromDateAfterToDate_ThrowsException() {
        // Given
        CreateRequest invalidRequest = new CreateRequest(
                1L, 2,
                LocalDate.of(2024, 1, 25), // After to date
                LocalDate.of(2024, 1, 20),
                "Reason"
        );

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            borrowRequestService.createBorrowRequest(invalidRequest, 1L);
        });

        assertEquals("From date cannot be after to date", exception.getMessage());
    }

    @Test
    void createBorrowRequest_FromDateInPast_ThrowsException() {
        // Given
        CreateRequest invalidRequest = new CreateRequest(
                1L, 2,
                LocalDate.now().minusDays(1), // Past date
                LocalDate.now().plusDays(5),
                "Reason"
        );

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            borrowRequestService.createBorrowRequest(invalidRequest, 1L);
        });

        assertEquals("From date cannot be in the past", exception.getMessage());
    }

    @Test
    void createBorrowRequest_EquipmentNotAvailable_ThrowsException() {
        // Given
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(equipmentBookingRepository.getTotalBookedQuantityForDate(any(), any())).thenReturn(9); // Only 1 available

        // When & Then
        EquipmentNotAvailableException exception = assertThrows(EquipmentNotAvailableException.class, () -> {
            borrowRequestService.createBorrowRequest(createRequest, 1L);
        });

        assertEquals("Not enough equipment available for the requested period", exception.getMessage());
    }

    @Test
    void approveRequest_Success() {
        // Given
        ApproveRequest approveRequest = new ApproveRequest(2L, "Approved for project work");

        when(borrowRequestRepository.findById(1L)).thenReturn(Optional.of(testBorrowRequest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testApprover));
        when(equipmentBookingRepository.getTotalBookedQuantityForDate(any(), any())).thenReturn(0);
        when(borrowRequestRepository.save(any(BorrowRequest.class))).thenReturn(testBorrowRequest);
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(testEquipment);

        // When
        BorrowRequestResponse response = borrowRequestService.approveRequest(1L, approveRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getRequestId());
        assertEquals(Status.APPROVED, testBorrowRequest.getStatus());
        assertEquals(testApprover, testBorrowRequest.getApprovedBy());
        assertEquals("Approved for project work", testBorrowRequest.getRemarks());

        verify(borrowRequestRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(borrowRequestRepository).save(testBorrowRequest);
        verify(equipmentRepository).save(testEquipment);
    }

    @Test
    void approveRequest_RequestNotFound_ThrowsException() {
        // Given
        ApproveRequest approveRequest = new ApproveRequest(2L, "Approved");
        when(borrowRequestRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            borrowRequestService.approveRequest(1L, approveRequest);
        });

        assertEquals("Borrow request not found with id: 1", exception.getMessage());
    }

    @Test
    void approveRequest_RequestNotPending_ThrowsException() {
        // Given
        testBorrowRequest.setStatus(Status.APPROVED);
        ApproveRequest approveRequest = new ApproveRequest(2L, "Approved");

        when(borrowRequestRepository.findById(1L)).thenReturn(Optional.of(testBorrowRequest));

        // When & Then
        InvalidOperationException exception = assertThrows(InvalidOperationException.class, () -> {
            borrowRequestService.approveRequest(1L, approveRequest);
        });

        assertEquals("Only pending requests can be approved", exception.getMessage());
    }

    @Test
    void approveRequest_ApproverNotFound_ThrowsException() {
        // Given
        ApproveRequest approveRequest = new ApproveRequest(999L, "Approved");

        when(borrowRequestRepository.findById(1L)).thenReturn(Optional.of(testBorrowRequest));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            borrowRequestService.approveRequest(1L, approveRequest);
        });

        assertEquals("User not found with id: 999", exception.getMessage());
    }

    @Test
    void approveRequest_EquipmentNotAvailable_ThrowsException() {
        // Given
        ApproveRequest approveRequest = new ApproveRequest(2L, "Approved");

        when(borrowRequestRepository.findById(1L)).thenReturn(Optional.of(testBorrowRequest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testApprover));
        when(equipmentBookingRepository.getTotalBookedQuantityForDate(any(), any())).thenReturn(9); // Not enough available

        // When & Then
        EquipmentNotAvailableException exception = assertThrows(EquipmentNotAvailableException.class, () -> {
            borrowRequestService.approveRequest(1L, approveRequest);
        });

        assertEquals("Equipment no longer available for the requested period", exception.getMessage());
    }

    @Test
    void rejectRequest_Success() {
        // Given
        RejectRequest rejectRequest = new RejectRequest("Equipment not available");

        when(borrowRequestRepository.findById(1L)).thenReturn(Optional.of(testBorrowRequest));
        when(borrowRequestRepository.save(any(BorrowRequest.class))).thenReturn(testBorrowRequest);

        // When
        BorrowRequestResponse response = borrowRequestService.rejectRequest(1L, rejectRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getRequestId());
        assertEquals(Status.REJECTED, testBorrowRequest.getStatus());
        assertEquals("Equipment not available", testBorrowRequest.getRemarks());

        verify(borrowRequestRepository).findById(1L);
        verify(borrowRequestRepository).save(testBorrowRequest);
    }

    @Test
    void rejectRequest_RequestNotFound_ThrowsException() {
        // Given
        RejectRequest rejectRequest = new RejectRequest("Not available");
        when(borrowRequestRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            borrowRequestService.rejectRequest(1L, rejectRequest);
        });

        assertEquals("Borrow request not found with id: 1", exception.getMessage());
    }

    @Test
    void rejectRequest_RequestNotPending_ThrowsException() {
        // Given
        testBorrowRequest.setStatus(Status.APPROVED);
        RejectRequest rejectRequest = new RejectRequest("Not available");

        when(borrowRequestRepository.findById(1L)).thenReturn(Optional.of(testBorrowRequest));

        // When & Then
        InvalidOperationException exception = assertThrows(InvalidOperationException.class, () -> {
            borrowRequestService.rejectRequest(1L, rejectRequest);
        });

        assertEquals("Only pending requests can be rejected", exception.getMessage());
    }

    @Test
    void markAsReturned_Success() {
        // Given
        testBorrowRequest.setStatus(Status.APPROVED);
        ReturnRequest returnRequest = new ReturnRequest(LocalDate.now(), "Good condition");

        List<EquipmentBooking> bookings = Arrays.asList(
                createMockBooking(1L),
                createMockBooking(2L)
        );

        when(borrowRequestRepository.findById(1L)).thenReturn(Optional.of(testBorrowRequest));
        when(equipmentBookingRepository.findByBorrowRequestRequestId(1L)).thenReturn(bookings);
        when(equipmentBookingRepository.saveAll(any())).thenReturn(bookings);
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(testEquipment);
        when(borrowRequestRepository.save(any(BorrowRequest.class))).thenReturn(testBorrowRequest);

        // When
        BorrowRequestResponse response = borrowRequestService.markAsReturned(1L, returnRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getRequestId());
        assertEquals(Status.RETURNED, testBorrowRequest.getStatus());
        assertEquals(LocalDate.now(), testBorrowRequest.getReturnDate());
        assertEquals("Good condition", testBorrowRequest.getConditionAfterUse());

        verify(borrowRequestRepository).findById(1L);
        verify(equipmentBookingRepository).findByBorrowRequestRequestId(1L);
        verify(equipmentBookingRepository).saveAll(bookings);
        verify(equipmentRepository).save(testEquipment);
        verify(borrowRequestRepository).save(testBorrowRequest);
    }

    @Test
    void markAsReturned_RequestNotFound_ThrowsException() {
        // Given
        ReturnRequest returnRequest = new ReturnRequest(LocalDate.now(), "Good condition");
        when(borrowRequestRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            borrowRequestService.markAsReturned(1L, returnRequest);
        });

        assertEquals("Borrow request not found with id: 1", exception.getMessage());
    }

    @Test
    void markAsReturned_RequestNotApproved_ThrowsException() {
        // Given
        testBorrowRequest.setStatus(Status.PENDING);
        ReturnRequest returnRequest = new ReturnRequest(LocalDate.now(), "Good condition");

        when(borrowRequestRepository.findById(1L)).thenReturn(Optional.of(testBorrowRequest));

        // When & Then
        InvalidOperationException exception = assertThrows(InvalidOperationException.class, () -> {
            borrowRequestService.markAsReturned(1L, returnRequest);
        });

        assertEquals("Only approved requests can be marked as returned", exception.getMessage());
    }

    @Test
    void getRequestById_Success() {
        // Given
        when(borrowRequestRepository.findById(1L)).thenReturn(Optional.of(testBorrowRequest));

        // When
        BorrowRequestResponse response = borrowRequestService.getRequestById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getRequestId());
        assertEquals("Dell Laptop", response.getEquipmentName());
        assertEquals("John Doe", response.getUserName());

        verify(borrowRequestRepository).findById(1L);
    }

    @Test
    void getRequestById_RequestNotFound_ThrowsException() {
        // Given
        when(borrowRequestRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            borrowRequestService.getRequestById(1L);
        });

        assertEquals("Borrow request not found with id: 1", exception.getMessage());
    }

    @Test
    void getMyRequests_Success() {
        // Given
        List<BorrowRequest> requests = Arrays.asList(testBorrowRequest);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(borrowRequestRepository.findByRequestedBy(testUser)).thenReturn(requests);

        // When
        List<BorrowRequestResponse> responses = borrowRequestService.getMyRequests(1L);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getRequestId());

        verify(userRepository).findById(1L);
        verify(borrowRequestRepository).findByRequestedBy(testUser);
    }

    @Test
    void getMyRequests_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            borrowRequestService.getMyRequests(1L);
        });

        assertEquals("User not found with id: 1", exception.getMessage());
    }

    @Test
    void getPendingRequests_Success() {
        // Given
        List<BorrowRequest> requests = Arrays.asList(testBorrowRequest);
        when(borrowRequestRepository.findByStatus(Status.PENDING)).thenReturn(requests);

        // When
        List<BorrowRequestResponse> responses = borrowRequestService.getPendingRequests();

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(Status.PENDING.toString(), responses.get(0).getStatus());

        verify(borrowRequestRepository).findByStatus(Status.PENDING);
    }

    @Test
    void getRequestsWithFilters_Success() {
        // Given
        List<BorrowRequest> requests = Arrays.asList(testBorrowRequest);
        when(borrowRequestRepository.findRequestsWithFilters(Status.PENDING, 1L)).thenReturn(requests);

        // When
        List<BorrowRequestResponse> responses = borrowRequestService.getRequestsWithFilters(Status.PENDING, 1L);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getRequestId());

        verify(borrowRequestRepository).findRequestsWithFilters(Status.PENDING, 1L);
    }

    private EquipmentBooking createMockBooking(Long bookingId) {
        EquipmentBooking booking = new EquipmentBooking();
        booking.setBookingId(bookingId);
        booking.setBorrowRequest(testBorrowRequest);
        booking.setEquipment(testEquipment);
        booking.setBookingDate(LocalDate.of(2024, 1, 15));
        booking.setQuantity(2);
        booking.setStatus(EquipmentBooking.Status.ACTIVE);
        return booking;
    }
}
