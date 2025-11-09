package com.school.equipment.service;

import com.school.equipment.dto.equipment.EquipmentCreateRequest;
import com.school.equipment.dto.equipment.EquipmentCreateResponse;
import com.school.equipment.dto.equipment.EquipmentResponse;
import com.school.equipment.dto.equipment.EquipmentUpdateRequest;
import com.school.equipment.entity.Equipment;
import com.school.equipment.entity.User;
import com.school.equipment.exception.ResourceNotFoundException;
import com.school.equipment.repository.EquipmentRepository;
import com.school.equipment.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EquipmentService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private UserRepository userRepository;

    public EquipmentCreateResponse createEquipment(EquipmentCreateRequest request, Long createdByUserId) {
        log.info("Creating new equipment - name: {}, category: {}, quantity: {}, createdBy: {}",
                request.getName(), request.getCategory(), request.getTotalQuantity(), createdByUserId);

        User createdBy = userRepository.findById(createdByUserId)
            .orElseThrow(() -> {
                log.error("Failed to create equipment - user not found: {}", createdByUserId);
                return new ResourceNotFoundException("User not found with id: " + createdByUserId);
            });

        Equipment equipment = new Equipment();
        equipment.setName(request.getName());
        equipment.setCategory(request.getCategory());
        equipment.setConditionStatus(request.getConditionStatus());
        equipment.setTotalQuantity(request.getTotalQuantity());
        equipment.setAvailableQuantity(request.getTotalQuantity()); // Initially all are available
        equipment.setAvailability(request.getAvailability());
        equipment.setDescription(request.getDescription());
        equipment.setCreatedBy(createdBy);

        Equipment savedEquipment = equipmentRepository.save(equipment);
        log.info("Equipment created successfully - equipmentId: {}, name: {}",
                savedEquipment.getEquipmentId(), savedEquipment.getName());

        return new EquipmentCreateResponse(
            savedEquipment.getEquipmentId(),
            "Equipment added successfully"
        );
    }

    public EquipmentResponse updateEquipment(Long equipmentId, EquipmentUpdateRequest request) {
        log.info("Updating equipment - equipmentId: {}", equipmentId);

        Equipment equipment = equipmentRepository.findById(equipmentId)
            .orElseThrow(() -> {
                log.error("Failed to update equipment - equipment not found: {}", equipmentId);
                return new ResourceNotFoundException("Equipment not found with id: " + equipmentId);
            });

        if (request.getName() != null) {
            equipment.setName(request.getName());
        }
        if (request.getCategory() != null) {
            equipment.setCategory(request.getCategory());
        }
        if (request.getConditionStatus() != null) {
            equipment.setConditionStatus(request.getConditionStatus());
        }
        equipment.setTotalQuantity(request.getTotalQuantity());
        equipment.setAvailableQuantity(request.getAvailableQuantity());

        if (request.getAvailability() != null) {
            equipment.setAvailability(request.getAvailability());
        }
        if (request.getDescription() != null) {
            equipment.setDescription(request.getDescription());
        }

        Equipment savedEquipment = equipmentRepository.save(equipment);
        log.info("Equipment updated successfully - equipmentId: {}, name: {}",
                savedEquipment.getEquipmentId(), savedEquipment.getName());
        return mapToResponse(savedEquipment);
    }

    public void deleteEquipment(Long equipmentId) {
        log.info("Deleting equipment - equipmentId: {}", equipmentId);

        if (!equipmentRepository.existsById(equipmentId)) {
            log.error("Failed to delete equipment - equipment not found: {}", equipmentId);
            throw new ResourceNotFoundException("Equipment not found with id: " + equipmentId);
        }
        equipmentRepository.deleteById(equipmentId);
        log.info("Equipment deleted successfully - equipmentId: {}", equipmentId);
    }

    public EquipmentResponse getEquipmentById(Long equipmentId) {
        log.debug("Fetching equipment by id: {}", equipmentId);

        Equipment equipment = equipmentRepository.findById(equipmentId)
            .orElseThrow(() -> {
                log.error("Equipment not found: {}", equipmentId);
                return new ResourceNotFoundException("Equipment not found with id: " + equipmentId);
            });
        return mapToResponse(equipment);
    }

    public List<EquipmentResponse> getAllEquipment(String category, Boolean availableOnly, String search) {
        log.debug("Fetching all equipment - category: {}, availableOnly: {}, search: {}",
                category, availableOnly, search);

        List<Equipment> equipmentList = equipmentRepository.findEquipmentWithFilters(
            category,
            availableOnly != null ? availableOnly : false,
            search
        );

        log.info("Retrieved {} equipment items", equipmentList.size());

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
            equipment.getAvailability(),
            equipment.getDescription(),
            equipment.getCreatedBy() != null ? equipment.getCreatedBy().getFullName() : null,
            equipment.getCreatedAt() != null ? equipment.getCreatedAt().toString() : null,
            equipment.getUpdatedAt() != null ? equipment.getUpdatedAt().toString() : null
        );
    }
}
