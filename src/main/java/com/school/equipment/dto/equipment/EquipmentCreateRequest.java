package com.school.equipment.dto.equipment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentCreateRequest {
    @NotBlank(message = "Equipment name is required")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    private String conditionStatus = "Good";

    @NotNull(message = "Total quantity is required")
    @Min(value = 1, message = "Total quantity must be at least 1")
    private Integer totalQuantity;

    private String description;
}
