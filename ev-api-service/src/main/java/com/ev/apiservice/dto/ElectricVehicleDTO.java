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
 * Data Transfer Object for representing Electric Vehicle details in API responses and full updates.
 */
@Data
@Schema(description = "Detailed information about an electric vehicle.")
public class ElectricVehicleDTO {

    @NotBlank(message = "VIN cannot be blank")
    @Size(max = 10, message = "VIN must be up to 10 characters")
    @Schema(description = "Vehicle Identification Number (unique, max 10 characters).", example = "5YJSA1E2XP")
    private String vin;

    @Schema(description = "County where the vehicle is registered.", example = "King")
    private String county;

    @Schema(description = "City where the vehicle is registered.", example = "Seattle")
    private String city;

    @Schema(description = "State abbreviation (e.g., WA).", example = "WA")
    private String state;

    @Schema(description = "Postal code.", example = "98101")
    private String postalCode;

    @NotNull(message = "Model year cannot be null")
    @Schema(description = "Model year of the vehicle.", example = "2023")
    private Integer modelYear;

    @NotBlank(message = "Make cannot be blank")
    @Schema(description = "Manufacturer of the vehicle.", example = "TESLA")
    private String make;

    @NotBlank(message = "Model cannot be blank")
    @Schema(description = "Model of the vehicle.", example = "Model Y")
    private String model;

    @Schema(description = "Type of electric vehicle (e.g., BEV, PHEV).", example = "Battery Electric Vehicle (BEV)")
    private String electricVehicleType;

    @Schema(description = "Clean Alternative Fuel Vehicle (CAFV) eligibility status.", example = "Clean Alternative Fuel Vehicle Eligible")
    private String cafvEligibilityStatus;

    @PositiveOrZero(message = "Electric range must be zero or positive")
    @Schema(description = "Electric range in miles.", example = "303")
    private Integer electricRange;

    @PositiveOrZero(message = "Base MSRP must be zero or positive")
    @Schema(description = "Base Manufacturer's Suggested Retail Price.", example = "50490.00")
    private BigDecimal baseMSRP;

    @Schema(description = "Legislative district identifier.", example = "36")
    private String legislativeDistrict;

    @NotNull(message = "DOL Vehicle ID cannot be null")
    @Schema(description = "Department of Licensing Vehicle ID (unique).", example = "220663008")
    private Long dolVehicleId;

    @Valid // Enables validation of the nested PointDTO object
    @Schema(description = "Geographic location (longitude, latitude) of the vehicle.")
    private PointDTO vehicleLocation;

    @Schema(description = "Electric utility provider.", example = "CITY OF SEATTLE - (WA)|CITY OF TACOMA - (WA)")
    private String electricUtility;

    @Schema(description = "2020 Census Tract identifier.", example = "53033005600")
    private Long censusTract2020;
}