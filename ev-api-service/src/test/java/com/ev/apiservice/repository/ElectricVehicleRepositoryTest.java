package com.ev.apiservice.repository;

import com.ev.apiservice.config.H2GisTestConfig;
import com.ev.apiservice.model.ElectricVehicle;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(H2GisTestConfig.class)
@ActiveProfiles("test")
@Transactional
public class ElectricVehicleRepositoryTest {

    @Autowired
    private ElectricVehicleRepository vehicleRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    @Sql(scripts = "/sql/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/sample-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findById_WhenVehicleExists_ShouldReturnVehicle() {
        // Act
        Optional<ElectricVehicle> result = vehicleRepository.findById("SAMPLE1234");  // Should match a VIN in sample-data.sql

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getVin()).isEqualTo("SAMPLE1234");
    }

    @Test
    @Sql(scripts = "/sql/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/sample-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findById_WhenVehicleDoesNotExist_ShouldReturnEmpty() {
        // Act
        Optional<ElectricVehicle> result = vehicleRepository.findById("NONEXISTENT");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @Sql(scripts = "/sql/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/sample-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findByMakeIgnoreCase_ShouldReturnMatchingVehicles() {
        // Act
        List<ElectricVehicle> result = vehicleRepository.findByMakeIgnoreCase("TESLA");  // Should match entries in sample-data.sql

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(vehicle -> vehicle.getMake().equalsIgnoreCase("TESLA"));
    }

    @Test
    @Sql(scripts = "/sql/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/sample-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findByModelYear_ShouldReturnMatchingVehicles() {
        // Act
        List<ElectricVehicle> result = vehicleRepository.findByModelYear(2023);  // Should match entries in sample-data.sql

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(vehicle -> vehicle.getModelYear() == 2023);
    }

    @Test
    @Sql(scripts = "/sql/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/sample-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findByMakeIgnoreCaseAndModelIgnoreCase_ShouldReturnMatchingVehicles() {
        // Act
        List<ElectricVehicle> result = vehicleRepository.findByMakeIgnoreCaseAndModelIgnoreCase("TESLA", "Model 3");

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(vehicle ->
                vehicle.getMake().equalsIgnoreCase("TESLA") &&
                        vehicle.getModel().equalsIgnoreCase("Model 3"));
    }

    @Test
    @Sql(scripts = "/sql/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/sample-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void updateBaseMsrpForMakeAndModel_ShouldUpdateMatchingVehicles() {
        // Arrange
        BigDecimal newMsrp = new BigDecimal("55000.00");

        // Act
        int updatedCount = vehicleRepository.updateBaseMsrpForMakeAndModel("TESLA", "Model 3", newMsrp);

        // Get updated vehicles to verify
        List<ElectricVehicle> updatedVehicles = vehicleRepository.findByMakeIgnoreCaseAndModelIgnoreCase("TESLA", "Model 3");

        // Assert
        assertThat(updatedCount).isPositive();  // Should have updated at least one record
        assertThat(updatedVehicles).isNotEmpty();
        assertThat(updatedVehicles).allMatch(vehicle -> vehicle.getBaseMSRP().compareTo(newMsrp) == 0);
    }

    @Test
    void save_ShouldPersistNewVehicle() {
        // Arrange
        ElectricVehicle newVehicle = new ElectricVehicle();
        newVehicle.setVin("TEST123456");
        newVehicle.setMake("TESLA");
        newVehicle.setModel("Model Y");
        newVehicle.setModelYear(2023);
        newVehicle.setBaseMSRP(new BigDecimal("60000.00"));
        newVehicle.setDolVehicleId(999999999L);  // Use a unique ID not in sample data
        newVehicle.setVehicleLocationPoint(geometryFactory.createPoint(new Coordinate(-122.33207, 47.60611)));

        // Act
        ElectricVehicle savedVehicle = vehicleRepository.save(newVehicle);
        boolean exists = vehicleRepository.existsById("TEST123456");

        // Assert
        assertThat(savedVehicle).isNotNull();
        assertThat(savedVehicle.getVin()).isEqualTo("TEST123456");
        assertThat(exists).isTrue();
    }

    @Test
    @Sql(scripts = "/sql/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/sample-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void deleteById_ShouldRemoveVehicle() {
        // Arrange
        String vin = "SAMPLE1234";  // Should match a VIN in sample-data.sql

        // Verify vehicle exists before deletion
        assertThat(vehicleRepository.existsById(vin)).isTrue();

        // Act
        vehicleRepository.deleteById(vin);

        // Assert
        assertThat(vehicleRepository.existsById(vin)).isFalse();
    }
}