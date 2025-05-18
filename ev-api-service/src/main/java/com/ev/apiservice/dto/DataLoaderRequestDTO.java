package com.ev.apiservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Data Transfer Object for data loading requests.
 */
@Data
@NoArgsConstructor
public class DataLoaderRequestDTO {

    @Schema(description = "CSV file to upload and process")
    @NotNull(message = "CSV file is required")
    private MultipartFile file;

    @Schema(description = "Number of records to process in each batch (default: 1000)",
            example = "1000", defaultValue = "1000")
    @Min(value = 1, message = "Batch size must be at least 1")
    private int batchSize = 1000;
}