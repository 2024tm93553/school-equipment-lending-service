package com.school.equipment.controller;

import com.school.equipment.dto.equipment.CreateRequest;
import com.school.equipment.dto.equipment.CreateResponse;
import com.school.equipment.dto.equipment.EquipmentResponse;
import com.school.equipment.dto.equipment.UpdateRequest;
import com.school.equipment.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@CrossOrigin(origins = "*")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @Operation(
            summary = "Add new equipment",
            description = "Creates a new equipment item in the system",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Equipment created successfully",
                            content = @Content(schema = @Schema(implementation = CreateResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request",
                            content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<CreateResponse> addEquipment(
            @Parameter(description = "Equipment creation request", required = true)
            @Valid @RequestBody CreateRequest request) {

            Long userId = 1L;
            CreateResponse response = equipmentService.createEquipment(request, userId);
            return ResponseEntity.ok(response);

    }

    @Operation(
            summary = "Update equipment",
            description = "Updates an existing equipment item by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Equipment updated successfully",
                            content = @Content(schema = @Schema(implementation = EquipmentResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request",
                            content = @Content)
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<EquipmentResponse> updateEquipment(
            @Parameter(description = "Equipment ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Equipment update request", required = true)
            @Valid @RequestBody UpdateRequest request) {

            EquipmentResponse response = equipmentService.updateEquipment(id, request);
            return ResponseEntity.ok(response);

    }

    @Operation(
            summary = "Delete equipment",
            description = "Deletes an equipment item by ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Equipment deleted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request",
                            content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(
            @Parameter(description = "Equipment ID", required = true)
            @PathVariable Long id) {

            equipmentService.deleteEquipment(id);
            return ResponseEntity.noContent().build();

    }

    @Operation(
            summary = "Get equipment by ID",
            description = "Retrieves an equipment item by its ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Equipment retrieved successfully",
                            content = @Content(schema = @Schema(implementation = EquipmentResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Equipment not found",
                            content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<EquipmentResponse> getEquipmentById(
            @Parameter(description = "Equipment ID", required = true)
            @PathVariable Long id) {

            EquipmentResponse response = equipmentService.getEquipmentById(id);
            return ResponseEntity.ok(response);

    }

    @Operation(
            summary = "Get all equipment",
            description = "Retrieves all equipment items, optionally filtered by category, availability, or search term",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Equipment list retrieved successfully",
                            content = @Content(schema = @Schema(implementation = EquipmentResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request",
                            content = @Content)
            }
    )
    @GetMapping
    public ResponseEntity<List<EquipmentResponse>> getAllEquipment(
            @Parameter(description = "Filter by category")
            @RequestParam(required = false) String category,
            @Parameter(description = "Filter only available equipment")
            @RequestParam(required = false) Boolean availableOnly,
            @Parameter(description = "Search term for equipment name or description")
            @RequestParam(required = false) String search) {

            List<EquipmentResponse> response = equipmentService.getAllEquipment(category, availableOnly, search);
            return ResponseEntity.ok(response);

    }
}
