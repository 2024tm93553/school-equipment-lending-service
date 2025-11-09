package com.school.equipment.service;

import com.school.equipment.dto.dashboard.AvailabilityResponse;
import com.school.equipment.dto.dashboard.DateAvailability;
import com.school.equipment.dto.dashboard.RequestSummary;
import com.school.equipment.dto.equipment.EquipmentResponse;
import com.school.equipment.entity.Equipment;
import com.school.equipment.entity.Status;
import com.school.equipment.repository.BorrowRequestRepository;
import com.school.equipment.repository.EquipmentBookingRepository;
import com.school.equipment.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private EquipmentBookingRepository equipmentBookingRepository;

    @Autowired
    private BorrowRequestRepository borrowRequestRepository;

    @Autowired
    private EquipmentService equipmentService;

    public List<EquipmentResponse> getAllEquipmentWithAvailability(String category, Boolean availableOnly, String search) {
        return equipmentService.getAllEquipment(category, availableOnly, search);
    }

    public AvailabilityResponse getEquipmentAvailability(Long equipmentId, LocalDate fromDate, LocalDate toDate) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        List<DateAvailability> bookedList = new ArrayList<>();
        List<DateAvailability> availableList = new ArrayList<>();

        LocalDate date = fromDate;
        while (!date.isAfter(toDate)) {
            Integer bookedQuantity = equipmentBookingRepository.getTotalBookedQuantityForDate(equipmentId, date);
            if (bookedQuantity == null) {
                bookedQuantity = 0;
            }

            int availableQuantity = equipment.getTotalQuantity() - bookedQuantity;

            DateAvailability bookedAvailability = new DateAvailability();
            bookedAvailability.setDate(date);
            bookedAvailability.setBooked(bookedQuantity);
            bookedList.add(bookedAvailability);

            DateAvailability availableAvailabilityObj = new DateAvailability();
            availableAvailabilityObj.setDate(date);
            availableAvailabilityObj.setAvailable(availableQuantity);
            availableList.add(availableAvailabilityObj);

            date = date.plusDays(1);
        }

        return new AvailabilityResponse(
                equipment.getEquipmentId(),
                equipment.getName(),
                equipment.getTotalQuantity(),
                bookedList,
                availableList
        );
    }

    public RequestSummary getRequestSummary() {
        long totalRequests = borrowRequestRepository.count();
        long pendingRequests = borrowRequestRepository.findByStatus(Status.PENDING).size();
        long approvedRequests = borrowRequestRepository.findByStatus(Status.APPROVED).size();
        long returnedRequests = borrowRequestRepository.findByStatus(Status.RETURNED).size();
        long rejectedRequests = borrowRequestRepository.findByStatus(Status.REJECTED).size();

        return new RequestSummary(
                totalRequests,
                pendingRequests,
                approvedRequests,
                returnedRequests,
                rejectedRequests
        );
    }
}
