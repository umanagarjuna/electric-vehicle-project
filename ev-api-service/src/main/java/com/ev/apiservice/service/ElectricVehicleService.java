package com.ev.apiservice.service;

import com.ev.apiservice.dto.CreateElectricVehicleDTO;
import com.ev.apiservice.dto.ElectricVehicleDTO;
import com.ev.apiservice.dto.UpdateMsrpRequestDTO;
import com.ev.apiservice.mapper.ElectricVehicleMapper;
import com.ev.apiservice.model.ElectricVehicle;
import com.ev.apiservice.repository.ElectricVehicleRepository;
import io.micrometer.tracing.Span; // Import Span
import io.micrometer.tracing.Tracer; // Import Tracer
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Use jakarta.transaction.Transactional if preferred

import java.util.Optional;

/**
 * Service class containing business logic for electric vehicle operations.
 * Manages transactions and interacts with the repository and mapper.
 * Includes manual distributed tracing spans.
 */
@Service
@RequiredArgsConstructor // Lombok handles constructor injection for final fields
@Slf4j
public class ElectricVehicleService {

    private final ElectricVehicleRepository vehicleRepository;
    private final ElectricVehicleMapper vehicleMapper;
    private final Tracer tracer;

    /**
     * Retrieves a paginated list of all electric vehicles.
     * @param pageable Pagination information (page number, size, sort order).
     * @return A Page of ElectricVehicleDTOs.
     */
    @Transactional(readOnly = true) // Transaction is read-only for performance
    public Page<ElectricVehicleDTO> getAllVehicles(Pageable pageable) {
        // Start a new span for this service method
        Span span = tracer.nextSpan().name("ElectricVehicleService.getAllVehicles").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            log.info("Fetching all vehicles. Page: {}, Size: {}, Sort: {}",
                    pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

            span.tag("page.number", String.valueOf(pageable.getPageNumber()));
            span.tag("page.size", String.valueOf(pageable.getPageSize()));
            span.tag("page.sort", pageable.getSort().toString());


            Page<ElectricVehicle> entityPage = vehicleRepository.findAll(pageable);
            span.event("repository.findAll"); // Mark the repository call point

            Page<ElectricVehicleDTO> dtoPage = entityPage.map(vehicleMapper::toDTO);
            span.event("mapper.toDTO.page"); // Mark the mapping point

            return dtoPage;
        } catch (Exception e) {
            span.error(e); // Record exception on the span
            span.tag("exception", e.getClass().getName()); // Add exception type tag
            throw e; // Re-throw the exception
        } finally {
            span.end(); // End the span
        }
    }

    /**
     * Retrieves a single electric vehicle by its VIN.
     * @param vin The Vehicle Identification Number.
     * @return An Optional containing the ElectricVehicleDTO if found, or empty if not.
     */
    @Transactional(readOnly = true)
    public Optional<ElectricVehicleDTO> getVehicleByVin(String vin) {
        Span span = tracer.nextSpan().name("ElectricVehicleService.getVehicleByVin").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            log.info("Fetching vehicle by VIN: {}", vin);
            // Add tag for VIN prefix, being mindful of sensitivity
            if (vin != null && vin.length() >= 3) {
                span.tag("vin.prefix", vin.substring(0, 3));
            } else if (vin != null) {
                span.tag("vin.prefix", vin);
            } else {
                span.tag("vin", "null"); // Tag indicating null VIN
            }


            Optional<ElectricVehicle> vehicleOptional = vehicleRepository.findById(vin);
            span.event("repository.findById");

            // Add tag indicating if vehicle was found
            span.tag("vehicle.found", String.valueOf(vehicleOptional.isPresent()));


            Optional<ElectricVehicleDTO> dtoOptional = vehicleOptional.map(vehicleMapper::toDTO);
            if (dtoOptional.isPresent()) {
                span.event("mapper.toDTO");
            }

            return dtoOptional;
        } catch (Exception e) {
            span.error(e);
            span.tag("exception", e.getClass().getName());
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Creates a new electric vehicle record.
     * @param createDto DTO containing data for the new vehicle.
     * @return The created ElectricVehicleDTO.
     * @throws IllegalArgumentException if a vehicle with the same VIN already exists.
     */
    @Transactional // Default transaction (read-write)
    public ElectricVehicleDTO createVehicle(CreateElectricVehicleDTO createDto) {
        Span span = tracer.nextSpan().name("ElectricVehicleService.createVehicle").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            log.info("Attempting to create new vehicle with VIN: {}", createDto.getVin());
            // Add tag for VIN prefix
            if (createDto.getVin() != null && createDto.getVin().length() >= 3) {
                span.tag("vin.prefix", createDto.getVin().substring(0, 3));
            } else if (createDto.getVin() != null) {
                span.tag("vin.prefix", createDto.getVin());
            } else {
                span.tag("vin", "null");
            }


            span.event("checking.existence");
            if (vehicleRepository.existsById(createDto.getVin())) {
                String errorMessage = "Vehicle with VIN " + createDto.getVin() + " already exists.";
                log.warn(errorMessage);
                span.tag("vehicle.exists", "true");
                throw new IllegalArgumentException(errorMessage);
            }
            span.tag("vehicle.exists", "false");


            ElectricVehicle entity = vehicleMapper.toEntity(createDto);
            span.event("mapper.toEntity");

            ElectricVehicle savedEntity = vehicleRepository.save(entity);
            span.event("repository.save");

            log.info("Successfully created vehicle with VIN: {}", savedEntity.getVin());
            // Add tag for created vehicle's DOL ID prefix (mindful of sensitivity)
            if (savedEntity.getDolVehicleId() != null) {
                span.tag("created.dol.id.prefix", savedEntity.getDolVehicleId().toString().substring(0, Math.min(savedEntity.getDolVehicleId().toString().length(), 3)));
            }


            ElectricVehicleDTO createdDto = vehicleMapper.toDTO(savedEntity);
            span.event("mapper.toDTO");

            return createdDto;
        } catch (Exception e) {
            span.error(e);
            span.tag("exception", e.getClass().getName());
            // Add a tag indicating if the exception was a known business error
            if (e instanceof IllegalArgumentException) {
                span.tag("business.error", "true");
            }
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Updates an existing electric vehicle record identified by its VIN.
     * @param vin The VIN of the vehicle to update.
     * @param updateDto DTO containing the updated data.
     * @return The updated ElectricVehicleDTO.
     * @throws EntityNotFoundException if no vehicle is found with the given VIN.
     */
    @Transactional
    public ElectricVehicleDTO updateVehicle(String vin, ElectricVehicleDTO updateDto) {
        Span span = tracer.nextSpan().name("ElectricVehicleService.updateVehicle").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            log.info("Attempting to update vehicle with VIN: {}", vin);
            // Add tag for VIN prefix
            if (vin != null && vin.length() >= 3) {
                span.tag("vin.prefix", vin.substring(0, 3));
            } else if (vin != null) {
                span.tag("vin.prefix", vin);
            } else {
                span.tag("vin", "null");
            }


            span.event("fetching.existing");
            ElectricVehicle existingEntity = vehicleRepository.findById(vin)
                    .orElseThrow(() -> {
                        String errorMessage = "Vehicle not found with VIN: " + vin + " for update.";
                        log.warn(errorMessage);
                        span.tag("vehicle.found", "false");
                        return new EntityNotFoundException(errorMessage);
                    });
            span.tag("vehicle.found", "true");


            // If DTO VIN is present and different, log error and throw exception.
            if (updateDto.getVin() != null && !vin.equals(updateDto.getVin())) {
                String errorMessage = String.format("Path VIN (%s) does not match DTO VIN (%s). " +
                        "The resource identified by the path cannot be changed.", vin, updateDto.getVin());
                log.error(errorMessage);
                span.tag("vin.mismatch", "true");
                throw new IllegalArgumentException(errorMessage);
            }
            span.tag("vin.mismatch", "false");

            // The mapper will update fields of 'existingEntity' based on 'updateDto'.
            // The VIN of 'existingEntity' (the primary key) remains unchanged.
            span.event("mapper.updateEntityFromDto");
            vehicleMapper.updateEntityFromDto(updateDto, existingEntity);

            span.event("repository.save");
            ElectricVehicle updatedEntity = vehicleRepository.save(existingEntity);

            log.info("Successfully updated vehicle with VIN: {}", updatedEntity.getVin());
            // Add tag for updated vehicle's DOL ID prefix
            if (updatedEntity.getDolVehicleId() != null) {
                span.tag("updated.dol.id.prefix", updatedEntity.getDolVehicleId().toString().substring(0, Math.min(updatedEntity.getDolVehicleId().toString().length(), 3)));
            }

            span.event("mapper.toDTO");
            return vehicleMapper.toDTO(updatedEntity);
        } catch (Exception e) {
            span.error(e);
            span.tag("exception", e.getClass().getName());
            if (e instanceof EntityNotFoundException || e instanceof IllegalArgumentException) {
                span.tag("business.error", "true");
            }
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Deletes an electric vehicle record by its VIN.
     * @param vin The VIN of the vehicle to delete.
     * @throws EntityNotFoundException if no vehicle is found with the given VIN.
     */
    @Transactional
    public void deleteVehicle(String vin) {
        Span span = tracer.nextSpan().name("ElectricVehicleService.deleteVehicle").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            log.info("Attempting to delete vehicle with VIN: {}", vin);
            // Add tag for VIN prefix
            if (vin != null && vin.length() >= 3) {
                span.tag("vin.prefix", vin.substring(0, 3));
            } else if (vin != null) {
                span.tag("vin.prefix", vin);
            } else {
                span.tag("vin", "null");
            }

            span.event("checking.existence");
            if (!vehicleRepository.existsById(vin)) {
                String errorMessage = "Attempted to delete non-existent vehicle with VIN: " + vin;
                log.warn(errorMessage);
                span.tag("vehicle.exists", "false");
                throw new EntityNotFoundException(errorMessage);
            }
            span.tag("vehicle.exists", "true");

            span.event("repository.deleteById");
            vehicleRepository.deleteById(vin);
            log.info("Successfully deleted vehicle with VIN: {}", vin);

        } catch (Exception e) {
            span.error(e);
            span.tag("exception", e.getClass().getName());
            if (e instanceof EntityNotFoundException) {
                span.tag("business.error", "true");
            }
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Performs a batch update of the Base MSRP for all vehicles
     * matching a specific make and model.
     * @param msrpRequest DTO containing the make, model, and new Base MSRP.
     * @return The number of vehicle records updated.
     */
    @Transactional
    public int updateBaseMsrpForMakeAndModel(UpdateMsrpRequestDTO msrpRequest) {
        Span span = tracer.nextSpan().name("ElectricVehicleService.updateBaseMsrpForMakeAndModel").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            String make = msrpRequest.getMake();
            String model = msrpRequest.getModel();
            log.info("Attempting to update Base MSRP for Make: '{}', Model: '{}' to {}",
                    make, model, msrpRequest.getNewBaseMSRP());

            // Add tags for make and model
            if (make != null) span.tag("update.make", make);
            if (model != null) span.tag("update.model", model);
            if (msrpRequest.getNewBaseMSRP() != null) span.tag("update.new_msrp", msrpRequest.getNewBaseMSRP().toString());


            span.event("repository.updateBaseMsrpForMakeAndModel");
            // The repository query handles case-insensitivity for make and model.
            int updatedCount = vehicleRepository.updateBaseMsrpForMakeAndModel(
                    make,
                    model,
                    msrpRequest.getNewBaseMSRP()
            );
            span.tag("update.count", String.valueOf(updatedCount));


            log.info("Successfully updated Base MSRP for {} vehicles matching Make: '{}' and Model: '{}'.",
                    updatedCount, make, model);
            return updatedCount;
        } catch (Exception e) {
            span.error(e);
            span.tag("exception", e.getClass().getName());
            throw e;
        } finally {
            span.end();
        }
    }
}