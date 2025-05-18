package com.ev.apiservice.controller;

import com.ev.apiservice.dto.CreateElectricVehicleDTO;
import com.ev.apiservice.dto.ElectricVehicleDTO;
import com.ev.apiservice.dto.UpdateMsrpRequestDTO;
import com.ev.apiservice.service.ElectricVehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 *
 * REST Controller for managing Electric Vehicle population data.
 * Provides endpoints for CRUD operations and batch updates.
 */
@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Electric Vehicle API", description = "API for managing electric vehicle population data")
public class ElectricVehicleController {

    private final ElectricVehicleService vehicleService;

    @Operation(summary = "Get all electric vehicles (paginated)",
            description = "Retrieves a paginated list of electric vehicles. Supports sorting (e.g., 'vin,asc' or 'modelYear,desc') " +
                    "and pagination parameters ('page', 'size').")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of vehicles.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Page.class)))
    @GetMapping
    public ResponseEntity<Page<ElectricVehicleDTO>> getAllVehicles(
            @Parameter(description = "Pagination information (e.g., page=0&size=20&sort=vin,asc). " +
                    "Default size is 20, sorted by VIN ascending.")
            @PageableDefault(size = 20, sort = "vin") Pageable pageable) {
        log.info("Received GET request for all vehicles. Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<ElectricVehicleDTO> vehicles = vehicleService.getAllVehicles(pageable);
        return ResponseEntity.ok(vehicles);
    }

    @Operation(summary = "Get an electric vehicle by its VIN",
            description = "Retrieves detailed information for a specific electric vehicle using its Vehicle Identification Number (VIN).")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved vehicle.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ElectricVehicleDTO.class)))
    @ApiResponse(responseCode = "404", description = "Vehicle not found with the specified VIN.")
    @GetMapping("/{vin}")
    public ResponseEntity<ElectricVehicleDTO> getVehicleByVin(
            @Parameter(description = "VIN of the vehicle to be obtained.", required = true, example = "5YJSA1E2XP")
            @PathVariable String vin) {
        log.info("Received GET request for vehicle by VIN: {}", vin);
        return vehicleService.getVehicleByVin(vin)
                .map(ResponseEntity::ok) // If found, wrap in ResponseEntity.ok()
                .orElse(ResponseEntity.notFound().build()); // If not found, return 404
    }

    @Operation(summary = "Create a new electric vehicle record",
            description = "Adds a new electric vehicle to the database.")
    @ApiResponse(responseCode = "201", description = "Vehicle created successfully.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ElectricVehicleDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input data (e.g., validation error, VIN already exists).")
    @PostMapping
    public ResponseEntity<ElectricVehicleDTO> createVehicle(
            @Parameter(description = "Vehicle object to be created. VIN must be unique.", required = true,
                    content = @Content(schema = @Schema(implementation = CreateElectricVehicleDTO.class)))
            @Valid @RequestBody CreateElectricVehicleDTO createDto) {
        log.info("Received POST request to create vehicle with VIN: {}", createDto.getVin());
        ElectricVehicleDTO createdVehicle = vehicleService.createVehicle(createDto);
        return new ResponseEntity<>(createdVehicle, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing electric vehicle record",
            description = "Updates all fields of an existing electric vehicle identified by its VIN. This is a full update (PUT semantics).")
    @ApiResponse(responseCode = "200", description = "Vehicle updated successfully.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ElectricVehicleDTO.class)))
    @ApiResponse(responseCode = "404", description = "Vehicle not found with the specified VIN.")
    @ApiResponse(responseCode = "400", description = "Invalid input data (e.g., validation error).")
    @PutMapping("/{vin}")
    public ResponseEntity<ElectricVehicleDTO> updateVehicle(
            @Parameter(description = "VIN of the vehicle to be updated. This VIN is authoritative.", required = true, example = "5YJSA1E2XP")
            @PathVariable String vin,
            @Parameter(description = "Updated vehicle object. All fields will be considered for update.", required = true,
                    content = @Content(schema = @Schema(implementation = ElectricVehicleDTO.class)))
            @Valid @RequestBody ElectricVehicleDTO updateDto) {
        log.info("Received PUT request to update vehicle with VIN: {}", vin);

        // The VIN in the path is the identifier of the resource to update.
        // If the DTO contains a VIN, it should ideally match the path VIN or be ignored.
        // The service layer handles the update logic based on the path 'vin'.
        if (updateDto.getVin() != null && !vin.equals(updateDto.getVin())) {
            log.error("Path VIN ({}) does not match DTO VIN ({}). The resource identified by the path VIN will be updated. " +
                            "The VIN in the request body, if different, is typically ignored for PUT operations on a specific resource URL.",
                    vin, updateDto.getVin());
            throw new IllegalArgumentException("Path VIN (" + vin + ") must match DTO VIN (" + updateDto.getVin() + ")" +
                    " for PUT operations.");
        } else if (updateDto.getVin() == null) {
            // If VIN is not in DTO, set it from path to ensure consistency
            updateDto.setVin(vin);
        }

        ElectricVehicleDTO updatedVehicle = vehicleService.updateVehicle(vin, updateDto);
        return ResponseEntity.ok(updatedVehicle);
    }

    @Operation(summary = "Delete an electric vehicle record by its VIN",
            description = "Removes an electric vehicle record from the database using its VIN.")
    @ApiResponse(responseCode = "204", description = "Vehicle deleted successfully (No Content).")
    @ApiResponse(responseCode = "404", description = "Vehicle not found with the specified VIN.")
    @DeleteMapping("/{vin}")
    public ResponseEntity<Void> deleteVehicle(
            @Parameter(description = "VIN of the vehicle to be deleted.", required = true, example = "5YJSA1E2XP")
            @PathVariable String vin) {
        log.info("Received DELETE request for vehicle with VIN: {}", vin);
        vehicleService.deleteVehicle(vin);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content for successful deletion
    }

    @Operation(summary = "Update Base MSRP for vehicles of a specific make and model",
            description = "Performs a batch update on the Base MSRP for all vehicles matching the given make and model criteria.")
    @ApiResponse(responseCode = "200", description = "MSRP updated successfully. Response includes count of updated records.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(type="object", example = "{\"message\":\"Base MSRP updated successfully...\"," +
                            " \"make\":\"TESLA\", \"model\":\"Model Y\", \"updatedCount\":150}")))
    @ApiResponse(responseCode = "400", description = "Invalid input data (e.g., missing make, model, or newBaseMSRP).")
    @PatchMapping("/batch/msrp") // Using PATCH as it's a partial update to a collection of resources
    public ResponseEntity<Map<String, Object>> updateBaseMsrpForMakeAndModel(
            @Parameter(description = "Request containing make, model, and the new Base MSRP value.", required = true,
                    content = @Content(schema = @Schema(implementation = UpdateMsrpRequestDTO.class)))
            @Valid @RequestBody UpdateMsrpRequestDTO msrpRequest) {
        log.info("Received PATCH request to batch update MSRP for Make: '{}', Model: '{}' to {}",
                msrpRequest.getMake(), msrpRequest.getModel(), msrpRequest.getNewBaseMSRP());
        int updatedCount = vehicleService.updateBaseMsrpForMakeAndModel(msrpRequest);
        Map<String, Object> response = Map.of(
                "message", "Base MSRP updated successfully for vehicles matching criteria.",
                "make", msrpRequest.getMake(),
                "model", msrpRequest.getModel(),
                "updatedCount", updatedCount
        );
        return ResponseEntity.ok(response);
    }
}