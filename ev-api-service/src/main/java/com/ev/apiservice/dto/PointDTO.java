package com.ev.apiservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object for representing geographic point coordinates (longitude and latitude).
 * Used for API request and response bodies.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a geographic point with longitude and latitude.")
public class PointDTO {

    @Schema(description = "Longitude of the geographic point (X coordinate).", example = "-122.33207")
    private Double longitude;

    @Schema(description = "Latitude of the geographic point (Y coordinate).", example = "47.60611")
    private Double latitude;
}