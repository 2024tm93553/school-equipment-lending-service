package com.school.equipment.dto.borrow;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequest {
    @NotNull(message = "Return date is required")
    private LocalDate returnDate;

    private String conditionAfterUse;
}