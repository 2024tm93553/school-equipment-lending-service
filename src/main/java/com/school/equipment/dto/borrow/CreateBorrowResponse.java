package com.school.equipment.dto.borrow;

import com.school.equipment.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBorrowResponse {

        private Long requestId;
        private Status status;
        private String message;
}
