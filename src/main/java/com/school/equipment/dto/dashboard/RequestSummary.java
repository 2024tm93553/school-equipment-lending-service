package com.school.equipment.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestSummary {
    private Long totalRequests;
    private Long pendingRequests;
    private Long approvedRequests;
    private Long returnedRequests;
    private Long rejectedRequests;
}