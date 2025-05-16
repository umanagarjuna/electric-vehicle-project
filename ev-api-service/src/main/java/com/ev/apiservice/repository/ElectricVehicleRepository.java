package com.ev.apiservice.repository;

import com.ev.apiservice.model.ElectricVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spring Data JPA repository for {@link ElectricVehicle} entities.
 * Provides CRUD operations and methods for custom queries.
 * JpaSpecificationExecutor allows for dynamic query construction using Specifications.
 */
@Repository
public interface ElectricVehicleRepository extends
        JpaRepository<ElectricVehicle, String>, JpaSpecificationExecutor<ElectricVehicle> {

    /**
     * Finds vehicles by make, ignoring case.
     * @param make The make of the vehicle.
     * @return A list of matching vehicles.
     */
    List<ElectricVehicle> findByMakeIgnoreCase(String make);

    /**
     * Finds vehicles by model year.
     * @param year The model year.
     * @return A list of matching vehicles.
     */
    List<ElectricVehicle> findByModelYear(Integer year);

    /**
     * Finds vehicles by make and model, ignoring case for both.
     * @param make The make of the vehicle.
     * @param model The model of the vehicle.
     * @return A list of matching vehicles.
     */
    List<ElectricVehicle> findByMakeIgnoreCaseAndModelIgnoreCase(String make, String model);

    /**
     * Updates the baseMSRP for all vehicles matching a given make and model.
     * This query uses JPQL (Java Persistence Query Language).
     * The {@code @Modifying} annotation is required for queries that modify data (UPDATE, DELETE).
     * The operation should be transactional, typically managed at the service layer.
     * Using UPPER() for case-insensitive matching on make and model.
     *
     * @param make The make of the vehicles to update.
     * @param model The model of the vehicles to update.
     * @param newBaseMSRP The new Base MSRP value.
     * @return The number of records updated.
     */
    @Modifying
    @Query("UPDATE ElectricVehicle ev SET ev.baseMSRP = :newBaseMSRP WHERE UPPER(ev.make) = UPPER(:make) " +
            "AND UPPER(ev.model) = UPPER(:model)")
    int updateBaseMsrpForMakeAndModel(@Param("make") String make, @Param("model") String model,
                                      @Param("newBaseMSRP") BigDecimal newBaseMSRP);
}