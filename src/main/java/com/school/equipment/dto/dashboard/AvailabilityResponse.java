package com.school.equipment.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {
    private Long equipmentId;
    private String name;
    private Integer totalQuantity;
    private List<DateAvailability> booked;
    private List<DateAvailability> available;
}