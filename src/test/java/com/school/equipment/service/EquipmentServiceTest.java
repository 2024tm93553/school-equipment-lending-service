package com.school.equipment.service;

import com.school.equipment.dto.equipment.EquipmentCreateRequest;
import com.school.equipment.dto.equipment.EquipmentCreateResponse;
import com.school.equipment.dto.equipment.EquipmentResponse;
import com.school.equipment.dto.equipment.EquipmentUpdateRequest;
import com.school.equipment.entity.Equipment;
import com.school.equipment.entity.User;
import com.school.equipment.repository.EquipmentRepository;
import com.school.equipment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EquipmentService equipmentService;

    private User testUser;
    private Equipment testEquipment;
    private EquipmentCreateRequest createRequest;
    private EquipmentUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setFullName("Test Admin");

        testEquipment = new Equipment();
        testEquipment.setEquipmentId(1L);
        testEquipment.setName("Dell Laptop");
        testEquipment.setCategory("Laptops");
        testEquipment.setConditionStatus("Good");
        testEquipment.setTotalQuantity(10);
        testEquipment.setAvailableQuantity(8);
        testEquipment.setAvailability(true);
        testEquipment.setDescription("High-performance laptop");
        testEquipment.setCreatedBy(testUser);
        testEquipment.setCreatedAt(LocalDateTime.now());
        testEquipment.setUpdatedAt(LocalDateTime.now());

        createRequest = new EquipmentCreateRequest(
                "Dell Laptop",
                "Laptops",
                "Good",
                10,
                true,
                "High-performance laptop"
        );

        updateRequest = new EquipmentUpdateRequest(
                "Updated Laptop",
                "Laptops",
                "Excellent",
                15,
                12,
                true,
                "Updated description"
        );
    }

    @Test
    void createEquipment_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(testEquipment);

        // When
        EquipmentCreateResponse response = equipmentService.createEquipment(createRequest, 1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getEquipmentId());
        assertEquals("Equipment added successfully", response.getMessage());

        verify(userRepository).findById(1L);
        verify(equipmentRepository).save(any(Equipment.class));
    }

    @Test
    void createEquipment_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            equipmentService.createEquipment(createRequest, 1L);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(equipmentRepository, never()).save(any());
    }

    @Test
    void updateEquipment_Success() {
        // Given
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(testEquipment);

        // When
        EquipmentResponse response = equipmentService.updateEquipment(1L, updateRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getEquipmentId());
        assertEquals("Updated Laptop", testEquipment.getName());
        assertEquals("Excellent", testEquipment.getConditionStatus());
        assertEquals(15, testEquipment.getTotalQuantity());
        assertEquals(12, testEquipment.getAvailableQuantity());

        verify(equipmentRepository).findById(1L);
        verify(equipmentRepository).save(testEquipment);
    }

    @Test
    void updateEquipment_EquipmentNotFound_ThrowsException() {
        // Given
        when(equipmentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            equipmentService.updateEquipment(1L, updateRequest);
        });

        assertEquals("Equipment not found", exception.getMessage());
        verify(equipmentRepository).findById(1L);
        verify(equipmentRepository, never()).save(any());
    }

    @Test
    void updateEquipment_PartialUpdate_Success() {
        // Given
        EquipmentUpdateRequest partialRequest = new EquipmentUpdateRequest();
        partialRequest.setName("Partially Updated Name");
        // Other fields are null

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(testEquipment);

        // When
        EquipmentResponse response = equipmentService.updateEquipment(1L, partialRequest);

        // Then
        assertNotNull(response);
        assertEquals("Partially Updated Name", testEquipment.getName());
        // Other fields should remain unchanged
        assertEquals("Laptops", testEquipment.getCategory());
        assertEquals("Good", testEquipment.getConditionStatus());

        verify(equipmentRepository).findById(1L);
        verify(equipmentRepository).save(testEquipment);
    }

    @Test
    void deleteEquipment_Success() {
        // Given
        when(equipmentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(equipmentRepository).deleteById(1L);

        // When
        equipmentService.deleteEquipment(1L);

        // Then
        verify(equipmentRepository).existsById(1L);
        verify(equipmentRepository).deleteById(1L);
    }

    @Test
    void deleteEquipment_EquipmentNotFound_ThrowsException() {
        // Given
        when(equipmentRepository.existsById(1L)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            equipmentService.deleteEquipment(1L);
        });

        assertEquals("Equipment not found", exception.getMessage());
        verify(equipmentRepository).existsById(1L);
        verify(equipmentRepository, never()).deleteById(any());
    }

    @Test
    void getEquipmentById_Success() {
        // Given
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));

        // When
        EquipmentResponse response = equipmentService.getEquipmentById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getEquipmentId());
        assertEquals("Dell Laptop", response.getName());
        assertEquals("Laptops", response.getCategory());
        assertEquals("Good", response.getConditionStatus());
        assertEquals(10, response.getTotalQuantity());
        assertEquals(8, response.getAvailableQuantity());
        assertEquals(true, response.getAvailability());
        assertEquals("High-performance laptop", response.getDescription());
        assertEquals("Test Admin", response.getCreatedBy());

        verify(equipmentRepository).findById(1L);
    }

    @Test
    void getEquipmentById_EquipmentNotFound_ThrowsException() {
        // Given
        when(equipmentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            equipmentService.getEquipmentById(1L);
        });

        assertEquals("Equipment not found", exception.getMessage());
        verify(equipmentRepository).findById(1L);
    }

    @Test
    void getAllEquipment_NoFilters_Success() {
        // Given
        List<Equipment> equipmentList = Arrays.asList(testEquipment);
        when(equipmentRepository.findEquipmentWithFilters(null, false, null)).thenReturn(equipmentList);

        // When
        List<EquipmentResponse> responses = equipmentService.getAllEquipment(null, null, null);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Dell Laptop", responses.get(0).getName());

        verify(equipmentRepository).findEquipmentWithFilters(null, false, null);
    }

    @Test
    void getAllEquipment_WithFilters_Success() {
        // Given
        String category = "Laptops";
        Boolean availableOnly = true;
        String search = "Dell";

        List<Equipment> equipmentList = Arrays.asList(testEquipment);
        when(equipmentRepository.findEquipmentWithFilters(category, true, search)).thenReturn(equipmentList);

        // When
        List<EquipmentResponse> responses = equipmentService.getAllEquipment(category, availableOnly, search);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Dell Laptop", responses.get(0).getName());
        assertEquals("Laptops", responses.get(0).getCategory());

        verify(equipmentRepository).findEquipmentWithFilters(category, true, search);
    }

    @Test
    void getAllEquipment_EmptyResult_Success() {
        // Given
        when(equipmentRepository.findEquipmentWithFilters(any(), any(), any())).thenReturn(Arrays.asList());

        // When
        List<EquipmentResponse> responses = equipmentService.getAllEquipment(null, null, null);

        // Then
        assertNotNull(responses);
        assertEquals(0, responses.size());

        verify(equipmentRepository).findEquipmentWithFilters(null, false, null);
    }

    @Test
    void getAllEquipment_AvailableOnlyFalse_CallsWithFalse() {
        // Given
        Boolean availableOnly = false;
        when(equipmentRepository.findEquipmentWithFilters(null, false, null)).thenReturn(Arrays.asList());

        // When
        equipmentService.getAllEquipment(null, availableOnly, null);

        // Then
        verify(equipmentRepository).findEquipmentWithFilters(null, false, null);
    }
}
