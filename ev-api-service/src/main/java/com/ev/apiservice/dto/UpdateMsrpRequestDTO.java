package com.ev.apiservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for batch updating the Base MSRP of vehicles
 * matching a specific make and model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating Base MSRP for a batch of vehicles by make and model.")
public class UpdateMsrpRequestDTO {

    @NotBlank(message = "Make cannot be blank")
    @Schema(description = "The manufacturer of the vehicles whose MSRP will be updated.", example = "TESLA")
    private String make;

    @NotBlank(message = "Model cannot be blank")
    @Schema(description = "The model of the vehicles whose MSRP will be updated.", example = "Model Y")
    private String model;

    @NotNull(message = "New Base MSRP cannot be null")
    @PositiveOrZero(message = "New Base MSRP must be zero or positive")
    @Schema(description = "The new Base MSRP value to set for the matching vehicles.", example = "65000.00")
    private BigDecimal newBaseMSRP;
}
