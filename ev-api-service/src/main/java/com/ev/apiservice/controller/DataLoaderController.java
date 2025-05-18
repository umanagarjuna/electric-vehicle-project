package com.ev.apiservice.controller;

import com.ev.apiservice.service.DataLoaderJobService;
import com.ev.apiservice.service.DataLoaderJobService.JobStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for loading electric vehicle data from CSV files.
 */
@RestController
@RequestMapping("/api/v1/data-loader")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Data Loader API", description = "API for loading electric vehicle data from CSV files")
public class DataLoaderController {

    private final DataLoaderJobService jobService;

    @Operation(summary = "Load data from CSV file",
            description = "Upload a CSV file containing electric vehicle data for processing. " +
                    "The file is processed asynchronously and returns a job ID for status tracking.")
    @ApiResponse(responseCode = "202", description = "CSV file accepted for processing",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(type = "object", example = "{\"message\":\"Data loading job started successfully\", " +
                            "\"jobId\":\"550e8400-e29b-41d4-a716-446655440000\", " +
                            "\"statusEndpoint\":\"/api/v1/data-loader/job-status/550e8400-e29b-41d4-a716-446655440000\"}")))
    @ApiResponse(responseCode = "400", description = "Invalid or empty file")
    @ApiResponse(responseCode = "500", description = "Error processing file")
    @PostMapping(value = "/load-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> loadCsvData(
            @Parameter(description = "CSV file to upload and process", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Batch size for processing")
            @RequestParam(value = "batchSize", required = false, defaultValue = "1000") Integer batchSize) {

        log.info("Received CSV file: {} ({}B) with batch size: {}",
                file.getOriginalFilename(), file.getSize(), batchSize);

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Uploaded file is empty"));
        }

        try {
            // Create temp directory and save file with unique name
            Path tempDir = Files.createTempDirectory("ev-loader");
            String filename = file.getOriginalFilename();
            String extension = filename != null && filename.contains(".")
                    ? filename.substring(filename.lastIndexOf("."))
                    : ".csv";

            Path tempFile = Files.createTempFile(tempDir, "upload-", extension);
            file.transferTo(tempFile.toFile());

            log.info("Saved to: {}", tempFile);

            // Start processing job
            String jobId = jobService.startLoadJob(tempFile.toString(), batchSize);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Data loading job started successfully");
            response.put("jobId", jobId);
            response.put("statusEndpoint", "/api/v1/data-loader/job-status/" + jobId);
            response.put("originalFilename", filename);

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            log.error("Error processing CSV file", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to process CSV file");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Get job status",
            description = "Check the status of a data loading job")
    @ApiResponse(responseCode = "200", description = "Job status retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Job not found")
    @GetMapping("/job-status/{jobId}")
    public ResponseEntity<Map<String, Object>> getJobStatus(
            @Parameter(description = "ID of the job to check", required = true)
            @PathVariable String jobId) {

        JobStatus status = jobService.getJobStatus(jobId);

        if (status.getStatus() == JobStatus.Status.NOT_FOUND) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("jobId", jobId);
        response.put("status", status.getStatus().name());
        response.put("recordsProcessed", status.getRecordsProcessed());
        response.put("startTime", status.getStartTime());

        if (status.getStatus() == JobStatus.Status.RUNNING) {
            response.put("progress", status.getProgress());
        }

        if (status.getStatus() == JobStatus.Status.FAILED && status.getErrorMessage() != null) {
            response.put("errorMessage", status.getErrorMessage());
        }

        return ResponseEntity.ok(response);
    }
}