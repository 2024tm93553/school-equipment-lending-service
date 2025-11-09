package com.school.equipment.controller;

import com.school.equipment.dto.dashboard.AvailabilityResponse;
import com.school.equipment.dto.dashboard.RequestSummary;
import com.school.equipment.dto.equipment.EquipmentResponse;
import com.school.equipment.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/equipment")
    public ResponseEntity<List<EquipmentResponse>> getAllEquipmentWithAvailability(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean availableOnly,
            @RequestParam(required = false) String search) {
        try {
            List<EquipmentResponse> response =
                    dashboardService.getAllEquipmentWithAvailability(category, availableOnly, search);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/equipment/{id}/availability")
    public ResponseEntity<AvailabilityResponse> getEquipmentAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        try {
            AvailabilityResponse response =
                    dashboardService.getEquipmentAvailability(id, fromDate, toDate);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/requests/summary")
    public ResponseEntity<RequestSummary> getRequestSummary() {
        try {
            RequestSummary response = dashboardService.getRequestSummary();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
