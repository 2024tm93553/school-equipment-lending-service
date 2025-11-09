package com.school.equipment.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentResponse {
        private Long equipmentId;
        private String name;
        private String category;
        private String conditionStatus;
        private Integer totalQuantity;
        private Integer availableQuantity;
        private Boolean availability;
        private String description;
        private String createdBy;
        private String createdAt;
        private String updatedAt;

}
