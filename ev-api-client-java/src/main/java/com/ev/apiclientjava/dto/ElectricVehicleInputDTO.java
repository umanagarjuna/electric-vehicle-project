package com.ev.apiclientjava.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

/**
 * Data Transfer Object used by the client to send Electric Vehicle data
 * to the API for create or update operations. It can also be used to represent
 * vehicle data received from the API if the structure aligns.
 *
 * Using @JsonInclude(JsonInclude.Include.NON_NULL) means that fields with null values
 * will not be included in the serialized JSON. This can be useful for PATCH-like updates
 * if the server is designed to interpret missing fields as "no change".
 * For PUT (full update), all relevant fields should typically be provided.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElectricVehicleInputDTO {
    private String vin;
    private String county;
    private String city;
    private String state;
    private String postalCode;
    private Integer modelYear;
    private String make;
    private String model;
    private String electricVehicleType;
    private String cafvEligibilityStatus;
    private Integer electricRange;
    private BigDecimal baseMSRP;
    private String legislativeDistrict;
    private Long dolVehicleId;
    private PointInputDTO vehicleLocation; // Nested DTO for location
    private String electricUtility;
    private Long censusTract2020;

    /**
     * Default constructor, often required by libraries like Jackson for deserialization.
     */
    public ElectricVehicleInputDTO() {}

    // Standard Getters and Setters for all fields
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
    public String getCounty() { return county; }
    public void setCounty(String county) { this.county = county; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public Integer getModelYear() { return modelYear; }
    public void setModelYear(Integer modelYear) { this.modelYear = modelYear; }
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getElectricVehicleType() { return electricVehicleType; }
    public void setElectricVehicleType(String electricVehicleType) { this.electricVehicleType = electricVehicleType; }
    public String getCafvEligibilityStatus() { return cafvEligibilityStatus; }
    public void setCafvEligibilityStatus(String cafvEligibilityStatus) { this.cafvEligibilityStatus = cafvEligibilityStatus; }
    public Integer getElectricRange() { return electricRange; }
    public void setElectricRange(Integer electricRange) { this.electricRange = electricRange; }
    public BigDecimal getBaseMSRP() { return baseMSRP; }
    public void setBaseMSRP(BigDecimal baseMSRP) { this.baseMSRP = baseMSRP; }
    public String getLegislativeDistrict() { return legislativeDistrict; }
    public void setLegislativeDistrict(String legislativeDistrict) { this.legislativeDistrict = legislativeDistrict; }
    public Long getDolVehicleId() { return dolVehicleId; }
    public void setDolVehicleId(Long dolVehicleId) { this.dolVehicleId = dolVehicleId; }
    public PointInputDTO getVehicleLocation() { return vehicleLocation; }
    public void setVehicleLocation(PointInputDTO vehicleLocation) { this.vehicleLocation = vehicleLocation; }
    public String getElectricUtility() { return electricUtility; }
    public void setElectricUtility(String electricUtility) { this.electricUtility = electricUtility; }
    public Long getCensusTract2020() { return censusTract2020; }
    public void setCensusTract2020(Long censusTract2020) { this.censusTract2020 = censusTract2020; }
}