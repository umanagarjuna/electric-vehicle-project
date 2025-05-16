package com.ev.apiservice.service;

import com.ev.apiservice.dto.CreateElectricVehicleDTO;
import com.ev.apiservice.dto.ElectricVehicleDTO;
import com.ev.apiservice.dto.UpdateMsrpRequestDTO;
import com.ev.apiservice.mapper.ElectricVehicleMapper;
import com.ev.apiservice.model.ElectricVehicle;
import com.ev.apiservice.repository.ElectricVehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service class containing business logic for electric vehicle operations.
 * Manages transactions and interacts with the repository and mapper.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ElectricVehicleService {

    private final ElectricVehicleRepository vehicleRepository;
    private final ElectricVehicleMapper vehicleMapper;

    /**
     * Retrieves a paginated list of all electric vehicles.
     * @param pageable Pagination information (page number, size, sort order).
     * @return A Page of ElectricVehicleDTOs.
     */
    @Transactional(readOnly = true) // Transaction is read-only for performance
    public Page<ElectricVehicleDTO> getAllVehicles(Pageable pageable) {
        log.info("Fetching all vehicles. Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return vehicleRepository.findAll(pageable)
                .map(vehicleMapper::toDTO);
    }

    /**
     * Retrieves a single electric vehicle by its VIN.
     * @param vin The Vehicle Identification Number.
     * @return An Optional containing the ElectricVehicleDTO if found, or empty if not.
     */
    @Transactional(readOnly = true)
    public Optional<ElectricVehicleDTO> getVehicleByVin(String vin) {
        log.info("Fetching vehicle by VIN: {}", vin);
        return vehicleRepository.findById(vin)
                .map(vehicleMapper::toDTO);
    }

    /**
     * Creates a new electric vehicle record.
     * @param createDto DTO containing data for the new vehicle.
     * @return The created ElectricVehicleDTO.
     * @throws IllegalArgumentException if a vehicle with the same VIN already exists.
     */
    @Transactional // Default transaction (read-write)
    public ElectricVehicleDTO createVehicle(CreateElectricVehicleDTO createDto) {
        log.info("Attempting to create new vehicle with VIN: {}", createDto.getVin());
        if (vehicleRepository.existsById(createDto.getVin())) {
            String errorMessage = "Vehicle with VIN " + createDto.getVin() + " already exists.";
            log.warn(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        ElectricVehicle entity = vehicleMapper.toEntity(createDto);
        ElectricVehicle savedEntity = vehicleRepository.save(entity);
        log.info("Successfully created vehicle with VIN: {}", savedEntity.getVin());
        return vehicleMapper.toDTO(savedEntity);
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
        log.info("Attempting to update vehicle with VIN: {}", vin);
        ElectricVehicle existingEntity = vehicleRepository.findById(vin)
                .orElseThrow(() -> {
                    String errorMessage = "Vehicle not found with VIN: " + vin + " for update.";
                    log.warn(errorMessage);
                    return new EntityNotFoundException(errorMessage);
                });

        // If DTO VIN is present and different, log error and throw exception.
        if (updateDto.getVin() != null && !vin.equals(updateDto.getVin())) {
            String errorMessage = String.format("Path VIN (%s) does not match DTO VIN (%s). " +
                    "The resource identified by the path cannot be changed.", vin, updateDto.getVin());
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        // The mapper will update fields of 'existingEntity' based on 'updateDto'.
        // The VIN of 'existingEntity' (the primary key) remains unchanged.
        vehicleMapper.updateEntityFromDto(updateDto, existingEntity);
        ElectricVehicle updatedEntity = vehicleRepository.save(existingEntity);
        log.info("Successfully updated vehicle with VIN: {}", updatedEntity.getVin());
        return vehicleMapper.toDTO(updatedEntity);
    }

    /**
     * Deletes an electric vehicle record by its VIN.
     * @param vin The VIN of the vehicle to delete.
     * @throws EntityNotFoundException if no vehicle is found with the given VIN.
     */
    @Transactional
    public void deleteVehicle(String vin) {
        log.info("Attempting to delete vehicle with VIN: {}", vin);
        if (!vehicleRepository.existsById(vin)) {
            String errorMessage = "Attempted to delete non-existent vehicle with VIN: " + vin;
            log.warn(errorMessage);
            throw new EntityNotFoundException(errorMessage);
        }
        vehicleRepository.deleteById(vin);
        log.info("Successfully deleted vehicle with VIN: {}", vin);
    }

    /**
     * Performs a batch update of the Base MSRP for all vehicles
     * matching a specific make and model.
     * @param msrpRequest DTO containing the make, model, and new Base MSRP.
     * @return The number of vehicle records updated.
     */
    @Transactional
    public int updateBaseMsrpForMakeAndModel(UpdateMsrpRequestDTO msrpRequest) {
        String make = msrpRequest.getMake();
        String model = msrpRequest.getModel();
        log.info("Attempting to update Base MSRP for Make: '{}', Model: '{}' to {}",
                make, model, msrpRequest.getNewBaseMSRP());

        // The repository query handles case-insensitivity for make and model.
        int updatedCount = vehicleRepository.updateBaseMsrpForMakeAndModel(
                make,
                model,
                msrpRequest.getNewBaseMSRP()
        );
        log.info("Successfully updated Base MSRP for {} vehicles matching Make: '{}' and Model: '{}'.",
                updatedCount, make, model);
        return updatedCount;
    }
}