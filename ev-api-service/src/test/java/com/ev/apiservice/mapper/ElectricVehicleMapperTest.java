package com.ev.apiservice.mapper;

import com.ev.apiservice.dto.CreateElectricVehicleDTO;
import com.ev.apiservice.dto.ElectricVehicleDTO;
import com.ev.apiservice.dto.PointDTO;
import com.ev.apiservice.model.ElectricVehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class ElectricVehicleMapperTest {

    private ElectricVehicleMapper mapper;
    private GeometryFactory geometryFactory;
    private ElectricVehicle sampleEntity;
    private ElectricVehicleDTO sampleDTO;
    private CreateElectricVehicleDTO createDTO;

    @BeforeEach
    void setUp() {
        mapper = new ElectricVehicleMapper();
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

        // Setup test entity
        sampleEntity = new ElectricVehicle();
        sampleEntity.setVin("TEST123456");
        sampleEntity.setMake("TESLA");
        sampleEntity.setModel("Model 3");
        sampleEntity.setModelYear(2023);
        sampleEntity.setBaseMSRP(new BigDecimal("45000.00"));
        sampleEntity.setDolVehicleId(123456789L);
        sampleEntity.setCounty("King");
        sampleEntity.setCity("Seattle");
        sampleEntity.setState("WA");
        sampleEntity.setPostalCode("98101");
        sampleEntity.setElectricVehicleType("Battery Electric Vehicle (BEV)");
        sampleEntity.setCafvEligibilityStatus("Clean Alternative Fuel Vehicle Eligible");
        sampleEntity.setElectricRange(300);
        sampleEntity.setLegislativeDistrict("43");
        sampleEntity.setElectricUtility("SEATTLE CITY LIGHT");
        sampleEntity.setCensusTract2020(53033005600L);
        sampleEntity.setVehicleLocationPoint(geometryFactory.createPoint(new Coordinate(-122.33207, 47.60611)));

        // Setup test DTO
        sampleDTO = new ElectricVehicleDTO();
        sampleDTO.setVin("TEST123456");
        sampleDTO.setMake("TESLA");
        sampleDTO.setModel("Model 3");
        sampleDTO.setModelYear(2023);
        sampleDTO.setBaseMSRP(new BigDecimal("45000.00"));
        sampleDTO.setDolVehicleId(123456789L);
        sampleDTO.setCounty("King");
        sampleDTO.setCity("Seattle");
        sampleDTO.setState("WA");
        sampleDTO.setPostalCode("98101");
        sampleDTO.setElectricVehicleType("Battery Electric Vehicle (BEV)");
        sampleDTO.setCafvEligibilityStatus("Clean Alternative Fuel Vehicle Eligible");
        sampleDTO.setElectricRange(300);
        sampleDTO.setLegislativeDistrict("43");
        sampleDTO.setElectricUtility("SEATTLE CITY LIGHT");
        sampleDTO.setCensusTract2020(53033005600L);
        sampleDTO.setVehicleLocation(new PointDTO(-122.33207, 47.60611));

        // Setup create DTO
        createDTO = new CreateElectricVehicleDTO();
        createDTO.setVin("TEST123456");
        createDTO.setMake("TESLA");
        createDTO.setModel("Model 3");
        createDTO.setModelYear(2023);
        createDTO.setBaseMSRP(new BigDecimal("45000.00"));
        createDTO.setDolVehicleId(123456789L);
        createDTO.setCounty("King");
        createDTO.setCity("Seattle");
        createDTO.setState("WA");
        createDTO.setPostalCode("98101");
        createDTO.setElectricVehicleType("Battery Electric Vehicle (BEV)");
        createDTO.setCafvEligibilityStatus("Clean Alternative Fuel Vehicle Eligible");
        createDTO.setElectricRange(300);
        createDTO.setLegislativeDistrict("43");
        createDTO.setElectricUtility("SEATTLE CITY LIGHT");
        createDTO.setCensusTract2020(53033005600L);
        createDTO.setVehicleLocation(new PointDTO(-122.33207, 47.60611));
    }

    @Test
    void toDTO_WhenEntityIsNull_ShouldReturnNull() {
        // Act
        ElectricVehicleDTO result = mapper.toDTO(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void toDTO_WhenEntityHasAllFields_ShouldMapAllFields() {
        // Act
        ElectricVehicleDTO result = mapper.toDTO(sampleEntity);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getVin()).isEqualTo(sampleEntity.getVin());
        assertThat(result.getMake()).isEqualTo(sampleEntity.getMake());
        assertThat(result.getModel()).isEqualTo(sampleEntity.getModel());
        assertThat(result.getModelYear()).isEqualTo(sampleEntity.getModelYear());
        assertThat(result.getBaseMSRP()).isEqualTo(sampleEntity.getBaseMSRP());
        assertThat(result.getDolVehicleId()).isEqualTo(sampleEntity.getDolVehicleId());
        assertThat(result.getCounty()).isEqualTo(sampleEntity.getCounty());
        assertThat(result.getCity()).isEqualTo(sampleEntity.getCity());
        assertThat(result.getState()).isEqualTo(sampleEntity.getState());
        assertThat(result.getPostalCode()).isEqualTo(sampleEntity.getPostalCode());
        assertThat(result.getElectricVehicleType()).isEqualTo(sampleEntity.getElectricVehicleType());
        assertThat(result.getCafvEligibilityStatus()).isEqualTo(sampleEntity.getCafvEligibilityStatus());
        assertThat(result.getElectricRange()).isEqualTo(sampleEntity.getElectricRange());
        assertThat(result.getLegislativeDistrict()).isEqualTo(sampleEntity.getLegislativeDistrict());
        assertThat(result.getElectricUtility()).isEqualTo(sampleEntity.getElectricUtility());
        assertThat(result.getCensusTract2020()).isEqualTo(sampleEntity.getCensusTract2020());

        // Check point coordinates
        assertThat(result.getVehicleLocation()).isNotNull();
        assertThat(result.getVehicleLocation().getLongitude()).isEqualTo(sampleEntity.getVehicleLocationPoint().getX());
        assertThat(result.getVehicleLocation().getLatitude()).isEqualTo(sampleEntity.getVehicleLocationPoint().getY());
    }

    @Test
    void toDTO_WhenEntityHasNullPoint_ShouldMapWithNullPoint() {
        // Arrange
        sampleEntity.setVehicleLocationPoint(null);

        // Act
        ElectricVehicleDTO result = mapper.toDTO(sampleEntity);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getVehicleLocation()).isNull();
    }

    @Test
    void toEntity_WhenDtoIsNull_ShouldReturnNull() {
        // Act
        ElectricVehicle result = mapper.toEntity(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void toEntity_WhenDtoHasAllFields_ShouldMapAllFields() {
        // Act
        ElectricVehicle result = mapper.toEntity(createDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getVin()).isEqualTo(createDTO.getVin());
        assertThat(result.getMake()).isEqualTo(createDTO.getMake());
        assertThat(result.getModel()).isEqualTo(createDTO.getModel());
        assertThat(result.getModelYear()).isEqualTo(createDTO.getModelYear());
        assertThat(result.getBaseMSRP()).isEqualTo(createDTO.getBaseMSRP());
        assertThat(result.getDolVehicleId()).isEqualTo(createDTO.getDolVehicleId());
        assertThat(result.getCounty()).isEqualTo(createDTO.getCounty());
        assertThat(result.getCity()).isEqualTo(createDTO.getCity());
        assertThat(result.getState()).isEqualTo(createDTO.getState());
        assertThat(result.getPostalCode()).isEqualTo(createDTO.getPostalCode());
        assertThat(result.getElectricVehicleType()).isEqualTo(createDTO.getElectricVehicleType());
        assertThat(result.getCafvEligibilityStatus()).isEqualTo(createDTO.getCafvEligibilityStatus());
        assertThat(result.getElectricRange()).isEqualTo(createDTO.getElectricRange());
        assertThat(result.getLegislativeDistrict()).isEqualTo(createDTO.getLegislativeDistrict());
        assertThat(result.getElectricUtility()).isEqualTo(createDTO.getElectricUtility());
        assertThat(result.getCensusTract2020()).isEqualTo(createDTO.getCensusTract2020());

        // Check point coordinates
        assertThat(result.getVehicleLocationPoint()).isNotNull();
        assertThat(result.getVehicleLocationPoint().getX()).isEqualTo(createDTO.getVehicleLocation().getLongitude());
        assertThat(result.getVehicleLocationPoint().getY()).isEqualTo(createDTO.getVehicleLocation().getLatitude());
    }

    @Test
    void toEntity_WhenDtoHasNullPoint_ShouldMapWithNullPoint() {
        // Arrange
        createDTO.setVehicleLocation(null);

        // Act
        ElectricVehicle result = mapper.toEntity(createDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getVehicleLocationPoint()).isNull();
    }

    @Test
    void toEntity_WhenDtoHasPointWithNullCoordinates_ShouldMapWithNullPoint() {
        // Arrange
        createDTO.setVehicleLocation(new PointDTO(null, null));

        // Act
        ElectricVehicle result = mapper.toEntity(createDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getVehicleLocationPoint()).isNull();
    }

    @Test
    void updateEntityFromDto_WhenBothAreNull_ShouldDoNothing() {
        // Act - No exceptions should be thrown
        mapper.updateEntityFromDto(null, null);
    }

    @Test
    void updateEntityFromDto_WhenDtoIsNull_ShouldNotUpdateEntity() {
        // Arrange
        ElectricVehicle entity = new ElectricVehicle();
        entity.setVin("TEST123456");
        entity.setMake("ORIGINAL_MAKE");

        // Act
        mapper.updateEntityFromDto(null, entity);

        // Assert - Entity should remain unchanged
        assertThat(entity.getVin()).isEqualTo("TEST123456");
        assertThat(entity.getMake()).isEqualTo("ORIGINAL_MAKE");
    }

    @Test
    void updateEntityFromDto_WhenEntityIsNull_ShouldDoNothing() {
        // Act - No exceptions should be thrown
        mapper.updateEntityFromDto(sampleDTO, null);
    }

    @Test
    void updateEntityFromDto_WhenBothHaveValues_ShouldUpdateAllEntityFields() {
        // Arrange
        ElectricVehicle entity = new ElectricVehicle();
        entity.setVin("TEST123456"); // VIN should not change
        entity.setMake("ORIGINAL_MAKE");
        entity.setModel("ORIGINAL_MODEL");

        // Act
        mapper.updateEntityFromDto(sampleDTO, entity);

        // Assert
        assertThat(entity.getVin()).isEqualTo("TEST123456"); // VIN remains unchanged
        assertThat(entity.getMake()).isEqualTo(sampleDTO.getMake());
        assertThat(entity.getModel()).isEqualTo(sampleDTO.getModel());
        assertThat(entity.getModelYear()).isEqualTo(sampleDTO.getModelYear());
        assertThat(entity.getBaseMSRP()).isEqualTo(sampleDTO.getBaseMSRP());
        assertThat(entity.getCounty()).isEqualTo(sampleDTO.getCounty());
        assertThat(entity.getCity()).isEqualTo(sampleDTO.getCity());
        assertThat(entity.getState()).isEqualTo(sampleDTO.getState());
        assertThat(entity.getPostalCode()).isEqualTo(sampleDTO.getPostalCode());
        assertThat(entity.getElectricVehicleType()).isEqualTo(sampleDTO.getElectricVehicleType());
        assertThat(entity.getCafvEligibilityStatus()).isEqualTo(sampleDTO.getCafvEligibilityStatus());
        assertThat(entity.getElectricRange()).isEqualTo(sampleDTO.getElectricRange());
        assertThat(entity.getLegislativeDistrict()).isEqualTo(sampleDTO.getLegislativeDistrict());
        assertThat(entity.getElectricUtility()).isEqualTo(sampleDTO.getElectricUtility());
        assertThat(entity.getCensusTract2020()).isEqualTo(sampleDTO.getCensusTract2020());

        // Check point coordinates
        assertThat(entity.getVehicleLocationPoint()).isNotNull();
        assertThat(entity.getVehicleLocationPoint().getX()).isEqualTo(sampleDTO.getVehicleLocation().getLongitude());
        assertThat(entity.getVehicleLocationPoint().getY()).isEqualTo(sampleDTO.getVehicleLocation().getLatitude());
    }

    @Test
    void updateEntityFromDto_WhenDtoHasNullPoint_ShouldSetEntityPointToNull() {
        // Arrange
        ElectricVehicle entity = new ElectricVehicle();
        entity.setVin("TEST123456");
        entity.setVehicleLocationPoint(geometryFactory.createPoint(new Coordinate(0, 0)));

        ElectricVehicleDTO dto = new ElectricVehicleDTO();
        dto.setVin("TEST123456");
        dto.setVehicleLocation(null);

        // Act
        mapper.updateEntityFromDto(dto, entity);

        // Assert
        assertThat(entity.getVehicleLocationPoint()).isNull();
    }

    @Test
    void toEntity_WhenDtoHasPartiallyNullPoint_ShouldHandleGracefully() {
        // Arrange - PointDTO with one null coordinate
        CreateElectricVehicleDTO dto = new CreateElectricVehicleDTO();
        dto.setVin("TEST12345");
        dto.setMake("TESLA");
        dto.setModel("Model 3");

        // Create a PointDTO with one null coordinate
        PointDTO pointWithNullLat = new PointDTO(-122.33, null);
        dto.setVehicleLocation(pointWithNullLat);

        // Act
        ElectricVehicle result = mapper.toEntity(dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getVehicleLocationPoint()).isNull(); // Should be null if any coordinate is null
    }

    @Test
    void updateEntityFromDto_WithNullLocation_ShouldSetLocationToNull() {
        // Arrange
        ElectricVehicleDTO dto = new ElectricVehicleDTO();
        dto.setVin("TEST12345");
        dto.setVehicleLocation(null);

        ElectricVehicle entity = new ElectricVehicle();
        entity.setVin("TEST12345");
        entity.setVehicleLocationPoint(geometryFactory.createPoint(new Coordinate(-122.33, 47.60)));

        // Act
        mapper.updateEntityFromDto(dto, entity);

        // Assert
        assertThat(entity.getVehicleLocationPoint()).isNull();
    }

    @Test
    void updateEntityFromDto_WithPartiallyNullLocation_ShouldSetLocationToNull() {
        // Arrange
        ElectricVehicleDTO dto = new ElectricVehicleDTO();
        dto.setVin("TEST12345");
        dto.setVehicleLocation(new PointDTO(null, 47.60)); // Longitude is null

        ElectricVehicle entity = new ElectricVehicle();
        entity.setVin("TEST12345");
        entity.setVehicleLocationPoint(geometryFactory.createPoint(new Coordinate(-122.33, 47.60)));

        // Act
        mapper.updateEntityFromDto(dto, entity);

        // Assert
        assertThat(entity.getVehicleLocationPoint()).isNull();
    }

    @Test
    void toDTO_WhenEntityHasNullFields_ShouldMapToNullValues() {
        // Arrange
        ElectricVehicle entity = new ElectricVehicle();
        entity.setVin("TEST12345");
        // Leave most fields null

        // Act
        ElectricVehicleDTO result = mapper.toDTO(entity);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getVin()).isEqualTo("TEST12345");
        assertThat(result.getMake()).isNull();
        assertThat(result.getModel()).isNull();
        assertThat(result.getModelYear()).isNull();
        assertThat(result.getBaseMSRP()).isNull();
        assertThat(result.getVehicleLocation()).isNull();
    }
}