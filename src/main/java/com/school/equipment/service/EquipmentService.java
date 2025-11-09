package com.school.equipment.service;

import com.school.equipment.dto.equipment.CreateRequest;
import com.school.equipment.dto.equipment.CreateResponse;
import com.school.equipment.dto.equipment.EquipmentResponse;
import com.school.equipment.dto.equipment.UpdateRequest;
import com.school.equipment.entity.Equipment;
import com.school.equipment.entity.User;
import com.school.equipment.repository.EquipmentRepository;
import com.school.equipment.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipmentService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private UserRepository userRepository;

    public CreateResponse createEquipment(CreateRequest request, Long createdByUserId) {
        User createdBy = userRepository.findById(createdByUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Equipment equipment = new Equipment();
        equipment.setName(request.getName());
        equipment.setCategory(request.getCategory());
        equipment.setConditionStatus(request.getConditionStatus());
        equipment.setTotalQuantity(request.getTotalQuantity());
        equipment.setAvailableQuantity(request.getTotalQuantity()); // Initially all are available
        equipment.setDescription(request.getDescription());
        equipment.setCreatedBy(createdBy);

        Equipment savedEquipment = equipmentRepository.save(equipment);

        return new CreateResponse(
            savedEquipment.getEquipmentId(),
            "Equipment added successfully"
        );
    }

    public EquipmentResponse updateEquipment(Long equipmentId, UpdateRequest request) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
            .orElseThrow(() -> new RuntimeException("Equipment not found"));

        if (request.getName() != null) {
            equipment.setName(request.getName());
        }
        if (request.getCategory() != null) {
            equipment.setCategory(request.getCategory());
        }
        if (request.getConditionStatus() != null) {
            equipment.setConditionStatus(request.getConditionStatus());
        }
        if (request.getTotalQuantity() != null) {
            // Update available quantity proportionally
            int difference = request.getTotalQuantity() - equipment.getTotalQuantity();
            equipment.setTotalQuantity(request.getTotalQuantity());
            equipment.setAvailableQuantity(Math.max(0, equipment.getAvailableQuantity() + difference));
        }
        if (request.getDescription() != null) {
            equipment.setDescription(request.getDescription());
        }

        Equipment savedEquipment = equipmentRepository.save(equipment);
        return mapToResponse(savedEquipment);
    }

    public void deleteEquipment(Long equipmentId) {
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new RuntimeException("Equipment not found");
        }
        equipmentRepository.deleteById(equipmentId);
    }

    public EquipmentResponse getEquipmentById(Long equipmentId) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
            .orElseThrow(() -> new RuntimeException("Equipment not found"));
        return mapToResponse(equipment);
    }

    public List<EquipmentResponse> getAllEquipment(String category, Boolean availableOnly, String search) {
        List<Equipment> equipmentList = equipmentRepository.findEquipmentWithFilters(
            category,
            availableOnly != null ? availableOnly : false,
            search
        );

        return equipmentList.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private EquipmentResponse mapToResponse(Equipment equipment) {
        return new EquipmentResponse(
            equipment.getEquipmentId(),
            equipment.getName(),
            equipment.getCategory(),
            equipment.getConditionStatus(),
            equipment.getTotalQuantity(),
            equipment.getAvailableQuantity(),
            equipment.getDescription(),
            equipment.getCreatedBy() != null ? equipment.getCreatedBy().getFullName() : null,
            equipment.getCreatedAt() != null ? equipment.getCreatedAt().toString() : null,
            equipment.getUpdatedAt() != null ? equipment.getUpdatedAt().toString() : null
        );
    }
}
