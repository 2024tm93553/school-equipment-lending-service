package com.school.equipment.dto.borrow;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveRequest {
        @NotNull(message = "Approved by is required")
        private Long approvedBy;

        private String remarks;
}
