package com.school.equipment.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentCreateResponse {
        private Long equipmentId;
        private String message;
}
