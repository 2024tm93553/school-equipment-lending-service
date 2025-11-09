package com.school.equipment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.equipment.dto.equipment.EquipmentCreateRequest;
import com.school.equipment.dto.equipment.EquipmentCreateResponse;
import com.school.equipment.dto.equipment.EquipmentResponse;
import com.school.equipment.dto.equipment.EquipmentUpdateRequest;
import com.school.equipment.service.EquipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EquipmentControllerTest {

    @Mock
    private EquipmentService equipmentService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private EquipmentController equipmentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(equipmentController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addEquipment_Success() throws Exception {
        // Given
        EquipmentCreateRequest request = new EquipmentCreateRequest(
                "Dell Laptop",
                "Laptops",
                "Good",
                10,
                true,
                "High-performance laptop"
        );

        EquipmentCreateResponse response = new EquipmentCreateResponse(1L, "Equipment added successfully");

        when(authentication.getDetails()).thenReturn(1L);
        when(equipmentService.createEquipment(any(EquipmentCreateRequest.class), eq(1L)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/equipment")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipmentId").value(1L))
                .andExpect(jsonPath("$.message").value("Equipment added successfully"));

        verify(equipmentService).createEquipment(any(EquipmentCreateRequest.class), eq(1L));
    }

    @Test
    void getAllEquipment_Success() throws Exception {
        // Given
        List<EquipmentResponse> responses = Arrays.asList(
                new EquipmentResponse(1L, "Dell Laptop", "Laptops", "Good", 10, 8, true, "Description 1", "Admin", "2024-01-15T10:30:00", "2024-01-20T14:45:00"),
                new EquipmentResponse(2L, "MacBook Pro", "Laptops", "Excellent", 5, 3, true, "Description 2", "Admin", "2024-01-16T09:15:00", "2024-01-18T11:20:00")
        );

        when(equipmentService.getAllEquipment(null, null, null)).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/equipment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Dell Laptop"))
                .andExpect(jsonPath("$[1].name").value("MacBook Pro"));

        verify(equipmentService).getAllEquipment(null, null, null);
    }

    @Test
    void getAllEquipment_WithFilters_Success() throws Exception {
        // Given
        String category = "Laptops";
        Boolean availableOnly = true;
        String search = "Dell";

        List<EquipmentResponse> responses = Arrays.asList(
                new EquipmentResponse(1L, "Dell Laptop", "Laptops", "Good", 10, 8, true, "Description", "Admin", "2024-01-15T10:30:00", "2024-01-20T14:45:00")
        );

        when(equipmentService.getAllEquipment(category, availableOnly, search)).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/equipment")
                        .param("category", category)
                        .param("availableOnly", availableOnly.toString())
                        .param("search", search))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Dell Laptop"))
                .andExpect(jsonPath("$[0].category").value("Laptops"));

        verify(equipmentService).getAllEquipment(category, availableOnly, search);
    }
}
