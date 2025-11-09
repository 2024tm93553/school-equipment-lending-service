package com.school.equipment.dto.borrow;

import com.school.equipment.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRequestResponse {

        private Long requestId;
        private Long equipmentId;
        private String equipmentName;
        private Long userId;
        private String userName;
        private Integer quantity;
        private LocalDate fromDate;
        private LocalDate toDate;
        private LocalDate returnDate;
        private String reason;
        private Status status;
        private String remarks;
        private String conditionAfterUse;
        private String approvedBy;
        private String createdAt;
        private String updatedAt;

}
