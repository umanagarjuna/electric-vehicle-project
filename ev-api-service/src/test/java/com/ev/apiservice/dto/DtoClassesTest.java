package com.ev.apiservice.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

public class DtoClassesTest {

    @Test
    void testPointDtoEqualsAndHashCode() {
        // Test equals() and hashCode() for PointDTO
        PointDTO point1 = new PointDTO(-122.33207, 47.60611);
        PointDTO point2 = new PointDTO(-122.33207, 47.60611);
        PointDTO point3 = new PointDTO(-122.0, 47.0);

        assertThat(point1).isEqualTo(point2);
        assertThat(point1.hashCode()).isEqualTo(point2.hashCode());
        assertThat(point1).isNotEqualTo(point3);
        assertThat(point1).isNotEqualTo(null);
        assertThat(point1).isNotEqualTo(new Object());
    }

    @Test
    void testPointDtoToString() {
        PointDTO point = new PointDTO(-122.33207, 47.60611);
        String toString = point.toString();

        assertThat(toString).contains("-122.33207");
        assertThat(toString).contains("47.60611");
    }

    @Test
    void testUpdateMsrpRequestDtoEqualsAndHashCode() {
        UpdateMsrpRequestDTO dto1 = new UpdateMsrpRequestDTO("TESLA", "Model 3", new BigDecimal("45000.00"));
        UpdateMsrpRequestDTO dto2 = new UpdateMsrpRequestDTO("TESLA", "Model 3", new BigDecimal("45000.00"));
        UpdateMsrpRequestDTO dto3 = new UpdateMsrpRequestDTO("TESLA", "Model Y", new BigDecimal("55000.00"));

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1).isNotEqualTo(dto3);
    }

    @Test
    void testCreateElectricVehicleDtoSettersAndGetters() {
        CreateElectricVehicleDTO dto = new CreateElectricVehicleDTO();

        dto.setVin("TEST12345");
        dto.setMake("TESLA");
        dto.setModel("Model S");
        dto.setModelYear(2023);
        dto.setBaseMSRP(new BigDecimal("80000.00"));
        dto.setDolVehicleId(12345678L);
        dto.setElectricRange(300);
        dto.setPostalCode("98101");
        dto.setVehicleLocation(new PointDTO(-122.33, 47.60));

        assertThat(dto.getVin()).isEqualTo("TEST12345");
        assertThat(dto.getMake()).isEqualTo("TESLA");
        assertThat(dto.getModel()).isEqualTo("Model S");
        assertThat(dto.getModelYear()).isEqualTo(2023);
        assertThat(dto.getBaseMSRP()).isEqualTo(new BigDecimal("80000.00"));
        assertThat(dto.getDolVehicleId()).isEqualTo(12345678L);
        assertThat(dto.getElectricRange()).isEqualTo(300);
        assertThat(dto.getPostalCode()).isEqualTo("98101");
        assertThat(dto.getVehicleLocation().getLongitude()).isEqualTo(-122.33);
        assertThat(dto.getVehicleLocation().getLatitude()).isEqualTo(47.60);
    }

    @Test
    void testElectricVehicleDtoSettersAndGetters() {
        ElectricVehicleDTO dto = new ElectricVehicleDTO();

        dto.setVin("TEST12345");
        dto.setMake("TESLA");
        dto.setModel("Model S");
        dto.setModelYear(2023);
        dto.setBaseMSRP(new BigDecimal("80000.00"));
        dto.setDolVehicleId(12345678L);
        dto.setElectricRange(300);
        dto.setCafvEligibilityStatus("Clean Alternative Fuel Vehicle Eligible");
        dto.setElectricVehicleType("Battery Electric Vehicle (BEV)");
        dto.setLegislativeDistrict("43");
        dto.setCensusTract2020(53033005600L);

        assertThat(dto.getVin()).isEqualTo("TEST12345");
        assertThat(dto.getMake()).isEqualTo("TESLA");
        assertThat(dto.getModel()).isEqualTo("Model S");
        assertThat(dto.getModelYear()).isEqualTo(2023);
        assertThat(dto.getBaseMSRP()).isEqualTo(new BigDecimal("80000.00"));
        assertThat(dto.getDolVehicleId()).isEqualTo(12345678L);
        assertThat(dto.getElectricRange()).isEqualTo(300);
        assertThat(dto.getCafvEligibilityStatus()).isEqualTo("Clean Alternative Fuel Vehicle Eligible");
        assertThat(dto.getElectricVehicleType()).isEqualTo("Battery Electric Vehicle (BEV)");
        assertThat(dto.getLegislativeDistrict()).isEqualTo("43");
        assertThat(dto.getCensusTract2020()).isEqualTo(53033005600L);
    }
}