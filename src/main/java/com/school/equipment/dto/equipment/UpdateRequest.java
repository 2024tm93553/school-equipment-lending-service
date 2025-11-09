package com.school.equipment.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRequest {

        private String name;
        private String category;
        private String conditionStatus;
        private Integer totalQuantity;
        private String description;
}
