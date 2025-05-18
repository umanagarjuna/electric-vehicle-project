package com.ev.apiservice.model;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class ElectricVehicleModelTest {

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    void testElectricVehicleEqualsAndHashCode() {
        ElectricVehicle vehicle1 = new ElectricVehicle();
        vehicle1.setVin("TEST12345");

        ElectricVehicle vehicle2 = new ElectricVehicle();
        vehicle2.setVin("TEST12345");

        ElectricVehicle vehicle3 = new ElectricVehicle();
        vehicle3.setVin("DIFF12345");

        assertThat(vehicle1).isEqualTo(vehicle2);
        assertThat(vehicle1.hashCode()).isEqualTo(vehicle2.hashCode());
        assertThat(vehicle1).isNotEqualTo(vehicle3);
        assertThat(vehicle1).isNotEqualTo(null);
        assertThat(vehicle1).isNotEqualTo(new Object());
    }

    @Test
    void testElectricVehicleToString() {
        ElectricVehicle vehicle = new ElectricVehicle();
        vehicle.setVin("TEST12345");
        vehicle.setMake("TESLA");
        vehicle.setModel("Model 3");

        String toString = vehicle.toString();

        assertThat(toString).contains("TEST12345");
        assertThat(toString).contains("TESLA");
        assertThat(toString).contains("Model 3");
    }

    @Test
    void testElectricVehicleAllArgsConstructor() {
        ElectricVehicle vehicle = new ElectricVehicle(
                "TEST12345", "King", "Seattle", "WA", "98101",
                2023, "TESLA", "Model 3", "Battery Electric Vehicle (BEV)",
                "Clean Alternative Fuel Vehicle Eligible", 300, new BigDecimal("45000.00"),
                "43", 123456789L, geometryFactory.createPoint(new Coordinate(-122.33, 47.60)),
                "SEATTLE CITY LIGHT", 53033005600L
        );

        assertThat(vehicle.getVin()).isEqualTo("TEST12345");
        assertThat(vehicle.getCounty()).isEqualTo("King");
        assertThat(vehicle.getCity()).isEqualTo("Seattle");
        assertThat(vehicle.getState()).isEqualTo("WA");
        assertThat(vehicle.getPostalCode()).isEqualTo("98101");
        assertThat(vehicle.getModelYear()).isEqualTo(2023);
        assertThat(vehicle.getMake()).isEqualTo("TESLA");
        assertThat(vehicle.getModel()).isEqualTo("Model 3");
        assertThat(vehicle.getElectricVehicleType()).isEqualTo("Battery Electric Vehicle (BEV)");
        assertThat(vehicle.getCafvEligibilityStatus()).isEqualTo("Clean Alternative Fuel Vehicle Eligible");
        assertThat(vehicle.getElectricRange()).isEqualTo(300);
        assertThat(vehicle.getBaseMSRP()).isEqualTo(new BigDecimal("45000.00"));
        assertThat(vehicle.getLegislativeDistrict()).isEqualTo("43");
        assertThat(vehicle.getDolVehicleId()).isEqualTo(123456789L);
        assertThat(vehicle.getVehicleLocationPoint().getX()).isEqualTo(-122.33);
        assertThat(vehicle.getVehicleLocationPoint().getY()).isEqualTo(47.60);
        assertThat(vehicle.getElectricUtility()).isEqualTo("SEATTLE CITY LIGHT");
        assertThat(vehicle.getCensusTract2020()).isEqualTo(53033005600L);
    }

    @Test
    void testElectricVehicleSettersAndGetters() {
        ElectricVehicle vehicle = new ElectricVehicle();

        vehicle.setVin("TEST12345");
        vehicle.setCounty("King");
        vehicle.setCity("Seattle");
        vehicle.setState("WA");
        vehicle.setPostalCode("98101");
        vehicle.setModelYear(2023);
        vehicle.setMake("TESLA");
        vehicle.setModel("Model 3");
        vehicle.setElectricVehicleType("Battery Electric Vehicle (BEV)");
        vehicle.setCafvEligibilityStatus("Clean Alternative Fuel Vehicle Eligible");
        vehicle.setElectricRange(300);
        vehicle.setBaseMSRP(new BigDecimal("45000.00"));
        vehicle.setLegislativeDistrict("43");
        vehicle.setDolVehicleId(123456789L);
        vehicle.setVehicleLocationPoint(geometryFactory.createPoint(new Coordinate(-122.33, 47.60)));
        vehicle.setElectricUtility("SEATTLE CITY LIGHT");
        vehicle.setCensusTract2020(53033005600L);

        assertThat(vehicle.getVin()).isEqualTo("TEST12345");
        assertThat(vehicle.getCounty()).isEqualTo("King");
        assertThat(vehicle.getCity()).isEqualTo("Seattle");
        assertThat(vehicle.getState()).isEqualTo("WA");
        assertThat(vehicle.getPostalCode()).isEqualTo("98101");
        assertThat(vehicle.getModelYear()).isEqualTo(2023);
        assertThat(vehicle.getMake()).isEqualTo("TESLA");
        assertThat(vehicle.getModel()).isEqualTo("Model 3");
        assertThat(vehicle.getElectricVehicleType()).isEqualTo("Battery Electric Vehicle (BEV)");
        assertThat(vehicle.getCafvEligibilityStatus()).isEqualTo("Clean Alternative Fuel Vehicle Eligible");
        assertThat(vehicle.getElectricRange()).isEqualTo(300);
        assertThat(vehicle.getBaseMSRP()).isEqualTo(new BigDecimal("45000.00"));
        assertThat(vehicle.getLegislativeDistrict()).isEqualTo("43");
        assertThat(vehicle.getDolVehicleId()).isEqualTo(123456789L);
        assertThat(vehicle.getVehicleLocationPoint().getX()).isEqualTo(-122.33);
        assertThat(vehicle.getVehicleLocationPoint().getY()).isEqualTo(47.60);
        assertThat(vehicle.getElectricUtility()).isEqualTo("SEATTLE CITY LIGHT");
        assertThat(vehicle.getCensusTract2020()).isEqualTo(53033005600L);
    }
}