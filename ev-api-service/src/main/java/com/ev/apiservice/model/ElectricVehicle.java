package com.ev.apiservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;

/**
 * JPA Entity representing an electric vehicle record in the database.
 */
@Entity
@Table(name = "electric_vehicle_population")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElectricVehicle {

    @Id
    @NotNull(message = "VIN cannot be null")
    @Size(max = 10, message = "VIN must be up to 10 characters")
    @Column(name = "vin", length = 10, nullable = false)
    private String vin;

    @Column(name = "county")
    private String county;

    @Column(name = "city")
    private String city;

    @Column(name = "state", length = 50)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "model_year")
    private Integer modelYear;

    @Column(name = "make")
    private String make;

    @Column(name = "model")
    private String model;

    @Column(name = "electric_vehicle_type", length = 100)
    private String electricVehicleType;

    @Column(name = "cafv_eligibility_status")
    private String cafvEligibilityStatus;

    @Column(name = "electric_range")
    private Integer electricRange;

    @Column(name = "base_msrp", precision = 12, scale = 2)
    private BigDecimal baseMSRP;

    @Column(name = "legislative_district", length = 50)
    private String legislativeDistrict;

    /*
     *  Must be unique and not null
     */
    @NotNull(message = "DOL Vehicle ID cannot be null")
    @Column(name = "dol_vehicle_id", unique = true, nullable = false)
    private Long dolVehicleId;

    /*
     * Specifies the column definition for PostGIS geometry type.
     * SRID 4326 is standard for WGS84 (latitude/longitude).
     */
    @Column(name = "vehicle_location_point", columnDefinition="geometry(Point,4326)")
    private Point vehicleLocationPoint; // Uses org.locationtech.jts.geom.Point

    @Column(name = "electric_utility")
    private String electricUtility;

    @Column(name = "census_tract_2020")
    private Long censusTract2020;
}