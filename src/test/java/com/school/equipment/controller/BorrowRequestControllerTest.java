package com.school.equipment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.school.equipment.dto.borrow.*;
import com.school.equipment.entity.Status;
import com.school.equipment.service.BorrowRequestService;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BorrowRequestControllerTest {

    @Mock
    private BorrowRequestService borrowRequestService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BorrowRequestController borrowRequestController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(borrowRequestController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createBorrowRequest_Success() throws Exception {
        // Given
        CreateRequest request = new CreateRequest(
                1L,
                2,
                LocalDate.of(2024, 1, 15),
                LocalDate.of(2024, 1, 20),
                "Need for project work"
        );

        CreateResponse response = new CreateResponse(1L, Status.PENDING, "Borrow request created successfully");

        when(authentication.getDetails()).thenReturn(1L);
        when(borrowRequestService.createBorrowRequest(any(CreateRequest.class), eq(1L)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/requests")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(1L))
                .andExpect(jsonPath("$.message").value("Borrow request created successfully"));

        verify(borrowRequestService).createBorrowRequest(any(CreateRequest.class), eq(1L));
    }

    @Test
    void createBorrowRequest_InvalidRequest_BadRequest() throws Exception {
        // Given
        CreateRequest request = new CreateRequest(
                null, // Invalid null equipmentId
                0, // Invalid quantity
                null, // Invalid null fromDate
                null, // Invalid null toDate
                "Reason"
        );

        // When & Then
        mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestById_Success() throws Exception {
        // Given
        Long requestId = 1L;
        BorrowRequestResponse response = createMockBorrowRequestResponse(requestId);

        when(borrowRequestService.getRequestById(requestId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/requests/{id}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(requestId))
                .andExpect(jsonPath("$.equipmentName").value("Dell Laptop"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(borrowRequestService).getRequestById(requestId);
    }

    @Test
    void getRequestById_NotFound() throws Exception {
        // Given
        Long requestId = 999L;
        when(borrowRequestService.getRequestById(requestId))
                .thenThrow(new RuntimeException("Request not found"));

        // When & Then
        mockMvc.perform(get("/api/requests/{id}", requestId))
                .andExpect(status().isInternalServerError());

        verify(borrowRequestService).getRequestById(requestId);
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getMyRequests_Success() throws Exception {
        // Given
        List<BorrowRequestResponse> responses = Arrays.asList(
                createMockBorrowRequestResponse(1L),
                createMockBorrowRequestResponse(2L)
        );

        when(authentication.getDetails()).thenReturn(1L);
        when(borrowRequestService.getMyRequests(1L)).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/requests/my")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].requestId").value(1L))
                .andExpect(jsonPath("$[1].requestId").value(2L));

        verify(borrowRequestService).getMyRequests(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPendingRequests_Success() throws Exception {
        // Given
        List<BorrowRequestResponse> responses = Arrays.asList(
                createMockBorrowRequestResponse(1L)
        );

        when(borrowRequestService.getPendingRequests()).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/requests/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(borrowRequestService).getPendingRequests();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllRequests_NoFilters_Success() throws Exception {
        // Given
        List<BorrowRequestResponse> responses = Arrays.asList(
                createMockBorrowRequestResponse(1L),
                createMockBorrowRequestResponse(2L)
        );

        when(borrowRequestService.getRequestsWithFilters(null, null)).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(borrowRequestService).getRequestsWithFilters(null, null);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllRequests_WithFilters_Success() throws Exception {
        // Given
        String status = "PENDING";
        Long userId = 1L;

        List<BorrowRequestResponse> responses = Arrays.asList(
                createMockBorrowRequestResponse(1L)
        );

        when(borrowRequestService.getRequestsWithFilters(Status.PENDING, userId)).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/requests")
                        .param("status", status)
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(borrowRequestService).getRequestsWithFilters(Status.PENDING, userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveRequest_Success() throws Exception {
        // Given
        Long requestId = 1L;
        ApproveRequest request = new ApproveRequest(1L, "Approved for project work");
        BorrowRequestResponse response = createMockBorrowRequestResponse(requestId);
        response.setStatus(Status.APPROVED);

        when(borrowRequestService.approveRequest(eq(requestId), any(ApproveRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/requests/{id}/approve", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(requestId))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(borrowRequestService).approveRequest(eq(requestId), any(ApproveRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectRequest_Success() throws Exception {
        // Given
        Long requestId = 1L;
        RejectRequest request = new RejectRequest("Equipment not available");
        BorrowRequestResponse response = createMockBorrowRequestResponse(requestId);
        response.setStatus(Status.REJECTED);

        when(borrowRequestService.rejectRequest(eq(requestId), any(RejectRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/requests/{id}/reject", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(requestId))
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(borrowRequestService).rejectRequest(eq(requestId), any(RejectRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void markAsReturned_Success() throws Exception {
        // Given
        Long requestId = 1L;
        ReturnRequest request = new ReturnRequest(LocalDate.now(), "Good condition");
        BorrowRequestResponse response = createMockBorrowRequestResponse(requestId);
        response.setStatus(Status.RETURNED);

        when(borrowRequestService.markAsReturned(eq(requestId), any(ReturnRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/requests/{id}/return", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(requestId))
                .andExpect(jsonPath("$.status").value("RETURNED"));

        verify(borrowRequestService).markAsReturned(eq(requestId), any(ReturnRequest.class));
    }

    @Test
    @WithMockUser(roles = "LAB_ASSISTANT")
    void approveRequest_AsLabAssistant_Success() throws Exception {
        // Given
        Long requestId = 1L;
        ApproveRequest request = new ApproveRequest(1L, "Approved");
        BorrowRequestResponse response = createMockBorrowRequestResponse(requestId);
        response.setStatus(Status.APPROVED);

        when(borrowRequestService.approveRequest(eq(requestId), any(ApproveRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/requests/{id}/approve", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(borrowRequestService).approveRequest(eq(requestId), any(ApproveRequest.class));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void createBorrowRequest_AsTeacher_Success() throws Exception {
        // Given
        CreateRequest request = new CreateRequest(
                1L, 1, LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 20), "Teaching purpose"
        );
        CreateResponse response = new CreateResponse(1L, Status.PENDING, "Request created");

        when(authentication.getDetails()).thenReturn(1L);
        when(borrowRequestService.createBorrowRequest(any(CreateRequest.class), eq(1L)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/requests")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(1L));

        verify(borrowRequestService).createBorrowRequest(any(CreateRequest.class), eq(1L));
    }

    private BorrowRequestResponse createMockBorrowRequestResponse(Long requestId) {
        BorrowRequestResponse response = new BorrowRequestResponse();
        response.setRequestId(requestId);
        response.setEquipmentId(1L);
        response.setEquipmentName("Dell Laptop");
        response.setUserId(1L);
        response.setUserName("John Doe");
        response.setQuantity(2);
        response.setFromDate(LocalDate.of(2024, 1, 15));
        response.setToDate(LocalDate.of(2024, 1, 20));
        response.setStatus(Status.PENDING);
        response.setReason("Need for project work");
        response.setCreatedAt("2024-01-15T10:30:00");
        return response;
    }
}
