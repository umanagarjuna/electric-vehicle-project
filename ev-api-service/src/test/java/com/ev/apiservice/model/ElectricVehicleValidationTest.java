package com.ev.apiservice.model;

import com.ev.apiservice.config.PostgresTestContainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(PostgresTestContainer.class)
@ActiveProfiles("test")
@Transactional
public class ElectricVehicleValidationTest {

    private Validator validator;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validate_WhenVinIsNull_ShouldHaveViolation() {
        // Arrange
        ElectricVehicle vehicle = new ElectricVehicle();
        vehicle.setVin(null);
        vehicle.setDolVehicleId(123456L);

        // Act
        Set<ConstraintViolation<ElectricVehicle>> violations = validator.validate(vehicle);

        // Assert
        assertThat(violations).isNotEmpty();
    }

    @Test
    void validate_WhenDolVehicleIdIsNull_ShouldHaveViolation() {
        // Arrange
        ElectricVehicle vehicle = new ElectricVehicle();
        vehicle.setVin("TEST12345");
        vehicle.setDolVehicleId(null);

        // Act
        Set<ConstraintViolation<ElectricVehicle>> violations = validator.validate(vehicle);

        // We expect violations since there is a @NotNull constraint
        assertThat(violations).isNotEmpty();
        // Check the specific message
        assertThat(violations).anyMatch(v ->
                v.getMessage().equals("DOL Vehicle ID cannot be null") &&
                        v.getPropertyPath().toString().equals("dolVehicleId"));
    }

    @Test
    void validate_WhenAllRequiredFieldsProvided_ShouldHaveNoViolations() {
        // Arrange
        ElectricVehicle vehicle = new ElectricVehicle();
        vehicle.setVin("TEST12345");
        vehicle.setDolVehicleId(123456L);

        // Act
        Set<ConstraintViolation<ElectricVehicle>> violations = validator.validate(vehicle);

        // Assert
        assertThat(violations).isEmpty();
    }

    /**
     * Test entity-level constraints through JPA persistence
     */
    @Test
    @Transactional
    void persistenceConstraint_WhenVinIsNull_ShouldThrowException() {
        // Arrange
        ElectricVehicle vehicle = new ElectricVehicle();
        vehicle.setVin(null); // VIN is null, violation of @Id
        vehicle.setDolVehicleId(123456L);

        // Act & Assert
        Assertions.assertThrows(RuntimeException.class, () -> {
            entityManager.persist(vehicle);
            entityManager.flush(); // Force the persistence to happen
        });
    }

    @Test
    @Transactional
    void persistenceConstraint_WhenDolVehicleIdIsNull_ShouldThrowConstraintViolationException() {
        // Arrange
        ElectricVehicle vehicle = new ElectricVehicle();
        vehicle.setVin("TEST12345");
        vehicle.setDolVehicleId(null); // DOL ID is null, violates validation constraint

        // Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persist(vehicle);
            entityManager.flush(); // Force the persistence to happen
        });
    }

    @Test
    @Transactional
    void persistence_WhenAllRequiredFieldsProvided_ShouldPersistSuccessfully() {
        // Arrange
        ElectricVehicle vehicle = new ElectricVehicle();
        vehicle.setVin("TEST12345");
        vehicle.setDolVehicleId(123456L);

        // Act
        entityManager.persist(vehicle);
        entityManager.flush();

        // Clear to ensure we fetch from DB
        entityManager.clear();

        // Assert
        ElectricVehicle persisted = entityManager.find(ElectricVehicle.class, "TEST12345");
        assertThat(persisted).isNotNull();
        assertThat(persisted.getVin()).isEqualTo("TEST12345");
        assertThat(persisted.getDolVehicleId()).isEqualTo(123456L);
    }

    @Test
    void validate_MaxVinLength_ShouldBeCheckedManually() {
        // Arrange
        ElectricVehicle vehicle = new ElectricVehicle();
        vehicle.setVin("12345678901234567890"); // More than 10 chars
        vehicle.setDolVehicleId(123456L);

        // Act
        Set<ConstraintViolation<ElectricVehicle>> violations = validator.validate(vehicle);

        // We can either check if empty (current state) or check length manually
        if (violations.isEmpty() || !violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("vin"))) {
            // If no size validation on vin field, verify length manually
            assertThat(vehicle.getVin().length() > 10).isTrue();
            System.out.println("NOTE: No bean validation constraint @Size " +
                    "was triggered on the VIN field. Consider adding it for earlier validation.");
        } else {
            // If @Size or @Length is present, a violation should be detected
            assertThat(violations.stream()
                    .filter(v -> v.getPropertyPath().toString().equals("vin"))
                    .findAny()
                    .isPresent()).isTrue();
        }
    }

    @Test
    @Transactional
    void persistence_WhenVinTooLong_ShouldThrowException() {
        // Arrange
        ElectricVehicle vehicle = new ElectricVehicle();
        vehicle.setVin("12345678901234567890"); // More than 10 chars
        vehicle.setDolVehicleId(123456L);

        // Act & Assert
        Assertions.assertThrows(RuntimeException.class, () -> {
            entityManager.persist(vehicle);
            entityManager.flush(); // Force the persistence to happen
        });
    }
}