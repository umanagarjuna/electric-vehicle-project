package com.ev.apiservice.repository;

import com.ev.apiservice.config.PostgresTestContainer;
import com.ev.apiservice.model.ElectricVehicle;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(PostgresTestContainer.class)
@ActiveProfiles("test")
@Transactional
public class ElectricVehicleRepositoryTest {

    @Autowired
    private ElectricVehicleRepository vehicleRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/sample-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findById_WhenVehicleExists_ShouldReturnVehicle() {
        // Act
        Optional<ElectricVehicle> result = vehicleRepository.findById("SAMPLE1234");  // Should match a VIN in sample-data.sql

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getVin()).isEqualTo("SAMPLE1234");
    }

    @Test
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/sample-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findById_WhenVehicleDoesNotExist_ShouldReturnEmpty() {
        // Act
        Optional<ElectricVehicle> result = vehicleRepository.findById("NONEXISTENT");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/sample-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findByMakeIgnoreCase_ShouldReturnMatchingVehicles() {
        // Act
        List<ElectricVehicle> result = vehicleRepository.findByMakeIgnoreCase("TESLA");  // Should match entries in sample-data.sql

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(vehicle -> vehicle.getMake().equalsIgnoreCase("TESLA"));
    }

    @Test
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/sample-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findByModelYear_ShouldReturnMatchingVehicles() {
        // Act
        List<ElectricVehicle> result = vehicleRepository.findByModelYear(2023);  // Should match entries in sample-data.sql

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(vehicle -> vehicle.getModelYear() == 2023);
    }

    @Test
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
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
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
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
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
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

    @Test
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/sample-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findAll_WithPagingAndSorting_ShouldReturnSortedPage() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "modelYear", "make"));

        // Act
        Page<ElectricVehicle> result = vehicleRepository.findAll(pageRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3); // Should return first page with 3 items

        // Verify sorting (modelYear DESC, make DESC)
        List<ElectricVehicle> content = result.getContent();
        for (int i = 0; i < content.size() - 1; i++) {
            // Either the current item's modelYear is greater than the next
            // or if modelYears are equal, make should be lexicographically greater or equal
            ElectricVehicle current = content.get(i);
            ElectricVehicle next = content.get(i + 1);

            assertThat(current.getModelYear() >= next.getModelYear()).isTrue();
            if (current.getModelYear().equals(next.getModelYear())) {
                assertThat(current.getMake().compareTo(next.getMake()) >= 0).isTrue();
            }
        }
    }

    @Test
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void updateBaseMsrpForMakeAndModel_WhenNoMatches_ShouldReturnZero() {
        // Arrange
        String make = "NONEXISTENT";
        String model = "Nonexistent Model";
        BigDecimal newMsrp = new BigDecimal("99999.99");

        // Act
        int updatedCount = vehicleRepository.updateBaseMsrpForMakeAndModel(make, model, newMsrp);

        // Assert
        assertThat(updatedCount).isZero();
    }

    @Test
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void deleteById_WhenVehicleDoesNotExist_ShouldCompleteWithoutChangingData() {
        // Arrange
        String nonExistentId = "NONEXISTENT";
        assertThat(vehicleRepository.existsById(nonExistentId)).isFalse();

        // Act
        vehicleRepository.deleteById(nonExistentId); // This doesn't throw an exception

        // Assert - Should still not exist after the delete operation
        assertThat(vehicleRepository.existsById(nonExistentId)).isFalse();
    }

    @Test
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/sample-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findByMakeIgnoreCase_WithCaseMismatch_ShouldReturnMatchesIgnoringCase() {
        // Act - Use mixed case for search
        List<ElectricVehicle> results = vehicleRepository.findByMakeIgnoreCase("Tesla");

        // Assert
        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(v -> v.getMake().equalsIgnoreCase("TESLA"));
    }

    @Test
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/sample-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void existsById_ShouldReturnTrueForExistingVehicle() {
        // Act
        boolean exists = vehicleRepository.existsById("SAMPLE1234");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void existsById_ShouldReturnFalseForNonExistingVehicle() {
        // Act
        boolean exists = vehicleRepository.existsById("NONEXISTENT");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/sample-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void count_ShouldReturnCorrectNumberOfVehicles() {
        // Act
        long count = vehicleRepository.count();

        // Assert - Sample data has 7 vehicles
        assertThat(count).isEqualTo(7);
    }

    @Test
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void count_WithEmptyDatabase_ShouldReturnZero() {
        // Act
        long count = vehicleRepository.count();

        // Assert
        assertThat(count).isZero();
    }
}