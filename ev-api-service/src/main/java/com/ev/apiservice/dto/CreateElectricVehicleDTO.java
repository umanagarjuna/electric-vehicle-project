package com.ev.apiservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Data Transfer Object for creating a new Electric Vehicle record.
 * Contains fields necessary for vehicle creation, with appropriate validation.
 */
@Data
@Schema(description = "Payload for creating a new electric vehicle record.")
public class CreateElectricVehicleDTO {

    @NotBlank(message = "VIN cannot be blank")
    @Size(max = 10, message = "VIN must be up to 10 characters")
    @Schema(description = "Vehicle Identification Number (unique, max 10 characters).", example = "KM8K33AGXL")
    private String vin;

    @Schema(description = "County of registration.", example = "Pierce")
    private String county;

    @Schema(description = "City of registration.", example = "Tacoma")
    private String city;

    @Schema(description = "State of registration.", example = "WA")
    private String state;

    @Schema(description = "Postal code.", example = "98407")
    private String postalCode;

    @NotNull(message = "Model year cannot be null")
    @Schema(description = "Vehicle model year.", example = "2020")
    private Integer modelYear;

    @NotBlank(message = "Make cannot be blank")
    @Schema(description = "Vehicle manufacturer.", example = "HYUNDAI")
    private String make;

    @NotBlank(message = "Model cannot be blank")
    @Schema(description = "Vehicle model.", example = "KONA")
    private String model;

    @Schema(description = "Electric vehicle type.", example = "Battery Electric Vehicle (BEV)")
    private String electricVehicleType;

    @Schema(description = "CAFV eligibility status.", example = "Clean Alternative Fuel Vehicle Eligible")
    private String cafvEligibilityStatus;

    @PositiveOrZero(message = "Electric range must be zero or positive")
    @Schema(description = "Electric range in miles.", example = "258")
    private Integer electricRange;

    @PositiveOrZero(message = "Base MSRP must be zero or positive")
    @Schema(description = "Base MSRP. Can be 0 if not applicable or unknown.", example = "0")
    private BigDecimal baseMSRP;

    @Schema(description = "Legislative district.", example = "27")
    private String legislativeDistrict;

    @NotNull(message = "DOL Vehicle ID cannot be null")
    @Schema(description = "Department of Licensing Vehicle ID.", example = "122371315")
    private Long dolVehicleId;

    @Valid // Validate nested PointDTO
    @Schema(description = "Geographic location of the vehicle.")
    private PointDTO vehicleLocation;

    @Schema(description = "Electric utility provider.", example = "BONNEVILLE POWER ADMINISTRATION||CITY OF TACOMA - (WA)||PENINSULA LIGHT COMPANY")
    private String electricUtility;

    @Schema(description = "2020 Census Tract ID.", example = "53053062400")
    private Long censusTract2020;
}