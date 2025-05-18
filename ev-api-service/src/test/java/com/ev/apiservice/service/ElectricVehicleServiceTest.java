package com.ev.apiservice.service;

import com.ev.apiservice.dto.CreateElectricVehicleDTO;
import com.ev.apiservice.dto.ElectricVehicleDTO;
import com.ev.apiservice.dto.PointDTO;
import com.ev.apiservice.dto.UpdateMsrpRequestDTO;
import com.ev.apiservice.mapper.ElectricVehicleMapper;
import com.ev.apiservice.model.ElectricVehicle;
import com.ev.apiservice.repository.ElectricVehicleRepository;
import io.micrometer.core.instrument.Timer;
import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.mockito.Mockito; // Import Mockito
import io.micrometer.core.instrument.Counter;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ElectricVehicleServiceTest {

    @Mock
    private ElectricVehicleRepository vehicleRepository;

    @Mock
    private ElectricVehicleMapper vehicleMapper;

    @InjectMocks
    private ElectricVehicleService vehicleService;

    @Mock // Mock the Tracer dependency
    private Tracer tracer;

    @Mock // Mock Span and its related objects
    private Span span;
    @Mock
    private Tracer.SpanInScope spanInScope;
    @Mock
    private TraceContext traceContext;
    // Add mocks for the Counter and Timer beans
    @Mock
    private Counter vehicleCreationCounter;
    @Mock
    private Counter vehicleUpdateCounter;
    @Mock
    private Counter vehicleDeletionCounter;
    @Mock
    private Counter batchUpdateCounter;
    @Mock
    private Timer apiRequestTimer; // Mock the apiRequestTimer

    private ElectricVehicle sampleEntity;
    private ElectricVehicleDTO sampleDTO;
    private CreateElectricVehicleDTO createDTO;
    private UpdateMsrpRequestDTO msrpRequestDTO;
    private GeometryFactory geometryFactory;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

        // Setup test data
        sampleEntity = new ElectricVehicle();
        sampleEntity.setVin("TEST123456");
        sampleEntity.setMake("TESLA");
        sampleEntity.setModel("Model 3");
        sampleEntity.setModelYear(2023);
        sampleEntity.setBaseMSRP(new BigDecimal("45000.00"));
        sampleEntity.setDolVehicleId(123456789L);
        sampleEntity.setVehicleLocationPoint(geometryFactory.createPoint(new Coordinate(-122.33207, 47.60611)));

        sampleDTO = new ElectricVehicleDTO();
        sampleDTO.setVin("TEST123456");
        sampleDTO.setMake("TESLA");
        sampleDTO.setModel("Model 3");
        sampleDTO.setModelYear(2023);
        sampleDTO.setBaseMSRP(new BigDecimal("45000.00"));
        sampleDTO.setDolVehicleId(123456789L);
        sampleDTO.setVehicleLocation(new PointDTO(-122.33207, 47.60611));

        createDTO = new CreateElectricVehicleDTO();
        createDTO.setVin("TEST123456");
        createDTO.setMake("TESLA");
        createDTO.setModel("Model 3");
        createDTO.setModelYear(2023);
        createDTO.setBaseMSRP(new BigDecimal("45000.00"));
        createDTO.setDolVehicleId(123456789L);
        createDTO.setVehicleLocation(new PointDTO(-122.33207, 47.60611));

        msrpRequestDTO = new UpdateMsrpRequestDTO();
        msrpRequestDTO.setMake("TESLA");
        msrpRequestDTO.setModel("Model 3");
        msrpRequestDTO.setNewBaseMSRP(new BigDecimal("47000.00"));

        pageable = PageRequest.of(0, 20);

        // --- Configure Tracer Mock ---
        // Mock tracer.nextSpan() to return our mock span
        // Use lenient() to avoid UnnecessaryStubbingException if some tests don't trigger this
        Mockito.lenient().when(tracer.nextSpan()).thenReturn(span);

        // Mock the methods called on the span, making them return the span itself for chaining
        Mockito.lenient().when(span.name(anyString())).thenReturn(span);
        Mockito.lenient().when(span.start()).thenReturn(span);
        Mockito.lenient().when(span.event(anyString())).thenReturn(span);
        Mockito.lenient().when(span.tag(anyString(), anyString())).thenReturn(span);
        Mockito.lenient().when(span.error(any(Throwable.class))).thenReturn(span);
        Mockito.lenient().doNothing().when(span).end(); // end() is often void


        // Mock tracer.withSpan() to return our mock spanInScope
        Mockito.lenient().when(tracer.withSpan(any())).thenReturn(spanInScope);
        // The SpanInScope also needs its close method mocked if used in try-with-resources
        Mockito.lenient().doNothing().when(spanInScope).close();

        // Mock tracer.currentSpan() if it's used (e.g., by logging framework with MDC)
        Mockito.lenient().when(tracer.currentSpan()).thenReturn(span);
        // Mock tracer.currentTraceContext() if it's used
        Mockito.lenient().when(tracer.currentTraceContext()).thenReturn(mock(CurrentTraceContext.class));
        // Mock traceContext().context()
        Mockito.lenient().when(tracer.currentTraceContext().context()).thenReturn(traceContext);


        // --- End Tracer Mock Configuration ---
    }

    @Test
    void getAllVehicles_ShouldReturnPageOfVehicleDTOs() {
        // Arrange
        Page<ElectricVehicle> entityPage = new PageImpl<>(List.of(sampleEntity));
        when(vehicleRepository.findAll(any(Pageable.class))).thenReturn(entityPage);
        when(vehicleMapper.toDTO(any(ElectricVehicle.class))).thenReturn(sampleDTO);

        // Act
        Page<ElectricVehicleDTO> result = vehicleService.getAllVehicles(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getVin()).isEqualTo("TEST123456");
        verify(vehicleRepository, times(1)).findAll(pageable);
        verify(vehicleMapper, times(1)).toDTO(sampleEntity);

        // Verify tracing interactions
        verify(tracer).nextSpan();
        verify(span).name("ElectricVehicleService.getAllVehicles");
        verify(span).start();
        verify(span).tag("page.number", "0");
        verify(span).tag("page.size", "20");
        verify(span).tag(eq("page.sort"), anyString()); // Sort toString() can vary
        verify(span).event("repository.findAll");
        verify(span).event("mapper.toDTO.page");
        verify(spanInScope).close(); // Verify SpanInScope is closed
        verify(span).end();
    }

    @Test
    void getVehicleByVin_WhenVehicleExists_ShouldReturnVehicleDTO() {
        // Arrange
        String vin = "TEST123456";
        when(vehicleRepository.findById(vin)).thenReturn(Optional.of(sampleEntity));
        when(vehicleMapper.toDTO(sampleEntity)).thenReturn(sampleDTO);

        // Act
        Optional<ElectricVehicleDTO> result = vehicleService.getVehicleByVin(vin);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getVin()).isEqualTo(vin);
        verify(vehicleRepository, times(1)).findById(vin);
        verify(vehicleMapper, times(1)).toDTO(sampleEntity);

        // Verify tracing interactions
        verify(tracer).nextSpan();
        verify(span).name("ElectricVehicleService.getVehicleByVin");
        verify(span).start();
        verify(span).tag("vin.prefix", "TES");
        verify(span).event("repository.findById");
        verify(span).tag("vehicle.found", "true");
        verify(span).event("mapper.toDTO");
        verify(spanInScope).close();
        verify(span).end();
    }

    @Test
    void getVehicleByVin_WhenVehicleDoesNotExist_ShouldReturnEmptyOptional() {
        // Arrange
        String vin = "NONEXISTENT";
        when(vehicleRepository.findById(vin)).thenReturn(Optional.empty());

        // Act
        Optional<ElectricVehicleDTO> result = vehicleService.getVehicleByVin(vin);

        // Assert
        assertThat(result).isEmpty();
        verify(vehicleRepository, times(1)).findById(vin);
        verify(vehicleMapper, never()).toDTO(any());

        // Verify tracing interactions
        verify(tracer).nextSpan();
        verify(span).name("ElectricVehicleService.getVehicleByVin");
        verify(span).start();
        verify(span).tag("vin.prefix", "NON");
        verify(span).event("repository.findById");
        verify(span).tag("vehicle.found", "false");
        // mapper.toDTO should NOT be called, so no event for it
        verify(spanInScope).close();
        verify(span).end();
    }

    @Test
    void createVehicle_WhenVehicleDoesNotExist_ShouldCreateAndReturnDTO() {
        // Arrange
        when(vehicleRepository.existsById(createDTO.getVin())).thenReturn(false);
        when(vehicleMapper.toEntity(createDTO)).thenReturn(sampleEntity);
        when(vehicleRepository.save(sampleEntity)).thenReturn(sampleEntity);
        when(vehicleMapper.toDTO(sampleEntity)).thenReturn(sampleDTO);

        // Act
        ElectricVehicleDTO result = vehicleService.createVehicle(createDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getVin()).isEqualTo("TEST123456");
        verify(vehicleRepository, times(1)).existsById(createDTO.getVin());
        verify(vehicleMapper, times(1)).toEntity(createDTO);
        verify(vehicleRepository, times(1)).save(sampleEntity);
        verify(vehicleMapper, times(1)).toDTO(sampleEntity);

        // Verify tracing interactions
        verify(tracer).nextSpan();
        verify(span).name("ElectricVehicleService.createVehicle");
        verify(span).start();
        verify(span).tag("vin.prefix", "TES");
        verify(span).event("checking.existence");
        verify(span).tag("vehicle.exists", "false");
        verify(span).event("mapper.toEntity");
        verify(span).event("repository.save");
        verify(span).tag("created.dol.id.prefix", "123"); // Assuming DOL ID prefix is 123
        verify(span).event("mapper.toDTO");
        verify(spanInScope).close();
        verify(span).end();
    }

    @Test
    void createVehicle_WhenVehicleExists_ShouldThrowIllegalArgumentException() {
        // Arrange
        when(vehicleRepository.existsById(createDTO.getVin())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> vehicleService.createVehicle(createDTO));
        assertThat(thrown.getMessage()).contains("Vehicle with VIN TEST123456 already exists.");

        verify(vehicleRepository, times(1)).existsById(createDTO.getVin());
        verify(vehicleMapper, never()).toEntity(any());
        verify(vehicleRepository, never()).save(any());

        // Verify tracing interactions
        verify(tracer).nextSpan();
        verify(span).name("ElectricVehicleService.createVehicle");
        verify(span).start();
        verify(span).tag("vin.prefix", "TES");
        verify(span).event("checking.existence");
        verify(span).tag("vehicle.exists", "true");
        verify(span).error(any(IllegalArgumentException.class)); // Verify error is recorded
        verify(span).tag("exception", IllegalArgumentException.class.getName()); // Verify exception tag
        verify(span).tag("business.error", "true"); // Verify business error tag
        verify(spanInScope).close();
        verify(span).end();
    }

    @Test
    void updateVehicle_WhenVehicleExists_ShouldUpdateAndReturnDTO() {
        // Arrange
        String vin = "TEST123456";
        ElectricVehicleDTO updateDTO = new ElectricVehicleDTO();
        updateDTO.setVin(vin);
        updateDTO.setMake("TESLA");
        updateDTO.setModel("Model 3 Performance");
        updateDTO.setModelYear(2023);
        updateDTO.setDolVehicleId(123456789L); // DOL ID might change in a real update, keep sample same for this test

        // Need a separate entity to represent the state *after* mapper update but before save
        ElectricVehicle updatedSampleEntity = new ElectricVehicle();
        updatedSampleEntity.setVin("TEST123456"); // VIN is primary key, shouldn't change
        updatedSampleEntity.setMake("TESLA");
        updatedSampleEntity.setModel("Model 3 Performance"); // Updated model
        updatedSampleEntity.setModelYear(2023);
        updatedSampleEntity.setBaseMSRP(new BigDecimal("45000.00")); // Assuming MSRP isn't updated here
        updatedSampleEntity.setDolVehicleId(123456789L); // Same DOL ID
        updatedSampleEntity.setVehicleLocationPoint(sampleEntity.getVehicleLocationPoint()); // Assuming location isn't updated here


        when(vehicleRepository.findById(vin)).thenReturn(Optional.of(sampleEntity));
        // Mock the mapper to perform the update on the existing entity instance
        doAnswer(invocation -> {
            ElectricVehicleDTO dto = invocation.getArgument(0);
            ElectricVehicle entity = invocation.getArgument(1);
            entity.setMake(dto.getMake());
            entity.setModel(dto.getModel());
            // ... map other fields you expect to be updated ...
            return null; // void method returns null
        }).when(vehicleMapper).updateEntityFromDto(updateDTO, sampleEntity);

        // Mock the repository save call to return the entity *after* it's updated
        when(vehicleRepository.save(sampleEntity)).thenReturn(sampleEntity); // vehicleRepository.save modifies and returns the same instance

        when(vehicleMapper.toDTO(sampleEntity)).thenReturn(updateDTO); // Mapper converts the updated entity back to DTO


        // Act
        ElectricVehicleDTO result = vehicleService.updateVehicle(vin, updateDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getVin()).isEqualTo(vin);
        assertThat(result.getModel()).isEqualTo("Model 3 Performance"); // Verify the expected update
        verify(vehicleRepository, times(1)).findById(vin);
        verify(vehicleMapper, times(1)).updateEntityFromDto(updateDTO, sampleEntity);
        verify(vehicleRepository, times(1)).save(sampleEntity);
        verify(vehicleMapper, times(1)).toDTO(sampleEntity);

        // Verify tracing interactions
        verify(tracer).nextSpan();
        verify(span).name("ElectricVehicleService.updateVehicle");
        verify(span).start();
        verify(span).tag("vin.prefix", "TES");
        verify(span).event("fetching.existing");
        verify(span).tag("vehicle.found", "true");
        verify(span).tag("vin.mismatch", "false");
        verify(span).event("mapper.updateEntityFromDto");
        verify(span).event("repository.save");
        verify(span).tag("updated.dol.id.prefix", "123"); // Assuming DOL ID prefix is 123
        verify(span).event("mapper.toDTO");
        verify(spanInScope).close();
        verify(span).end();
    }

    @Test
    void updateVehicle_WhenVehicleDoesNotExist_ShouldThrowEntityNotFoundException() {
        // Arrange
        String vin = "NONEXISTENT";
        ElectricVehicleDTO updateDTO = new ElectricVehicleDTO();
        updateDTO.setVin(vin);

        when(vehicleRepository.findById(vin)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> vehicleService.updateVehicle(vin, updateDTO));
        assertThat(thrown.getMessage()).contains("Vehicle not found with VIN: NONEXISTENT for update.");

        verify(vehicleRepository, times(1)).findById(vin);
        verify(vehicleMapper, never()).updateEntityFromDto(any(), any());
        verify(vehicleRepository, never()).save(any());

        // Verify tracing interactions
        verify(tracer).nextSpan();
        verify(span).name("ElectricVehicleService.updateVehicle");
        verify(span).start();
        verify(span).tag("vin.prefix", "NON");
        verify(span).event("fetching.existing");
        verify(span).tag("vehicle.found", "false");
        verify(span).error(any(EntityNotFoundException.class));
        verify(span).tag("exception", EntityNotFoundException.class.getName());
        verify(span).tag("business.error", "true");
        verify(spanInScope).close();
        verify(span).end();
    }

    @Test
    void updateVehicle_WhenVinMismatch_ShouldThrowIllegalArgumentException() {
        // Arrange
        String pathVin = "TEST123456";
        String dtoVin = "DIFFERENT789";
        ElectricVehicleDTO updateDTO = new ElectricVehicleDTO();
        updateDTO.setVin(dtoVin);

        // Need the existing entity to be found for the VIN mismatch check to occur after findById
        when(vehicleRepository.findById(pathVin)).thenReturn(Optional.of(sampleEntity));

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> vehicleService.updateVehicle(pathVin, updateDTO));
        assertThat(thrown.getMessage()).contains("Path VIN (TEST123456) does not match DTO VIN (DIFFERENT789).");

        verify(vehicleRepository, times(1)).findById(pathVin);
        verify(vehicleMapper, never()).updateEntityFromDto(any(), any());
        verify(vehicleRepository, never()).save(any());

        // Verify tracing interactions
        verify(tracer).nextSpan();
        verify(span).name("ElectricVehicleService.updateVehicle");
        verify(span).start();
        verify(span).tag("vin.prefix", "TES");
        verify(span).event("fetching.existing");
        verify(span).tag("vehicle.found", "true"); // Found the original by path VIN
        verify(span).tag("vin.mismatch", "true"); // Mismatch detected
        verify(span).error(any(IllegalArgumentException.class));
        verify(span).tag("exception", IllegalArgumentException.class.getName());
        verify(span).tag("business.error", "true");
        verify(spanInScope).close();
        verify(span).end();
    }

    @Test
    void deleteVehicle_WhenVehicleExists_ShouldDeleteVehicle() {
        // Arrange
        String vin = "TEST123456";
        when(vehicleRepository.existsById(vin)).thenReturn(true);
        doNothing().when(vehicleRepository).deleteById(vin);

        // Act
        vehicleService.deleteVehicle(vin);

        // Assert
        verify(vehicleRepository, times(1)).existsById(vin);
        verify(vehicleRepository, times(1)).deleteById(vin);

        // Verify tracing interactions
        verify(tracer).nextSpan();
        verify(span).name("ElectricVehicleService.deleteVehicle");
        verify(span).start();
        verify(span).tag("vin.prefix", "TES");
        verify(span).event("checking.existence");
        verify(span).tag("vehicle.exists", "true");
        verify(span).event("repository.deleteById");
        verify(spanInScope).close();
        verify(span).end();
    }

    @Test
    void deleteVehicle_WhenVehicleDoesNotExist_ShouldThrowEntityNotFoundException() {
        // Arrange
        String vin = "NONEXISTENT";
        when(vehicleRepository.existsById(vin)).thenReturn(false);

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> vehicleService.deleteVehicle(vin));
        assertThat(thrown.getMessage()).contains("Attempted to delete non-existent vehicle with VIN: NONEXISTENT");

        verify(vehicleRepository, times(1)).existsById(vin);
        verify(vehicleRepository, never()).deleteById(any());

        // Verify tracing interactions
        verify(tracer).nextSpan();
        verify(span).name("ElectricVehicleService.deleteVehicle");
        verify(span).start();
        verify(span).tag("vin.prefix", "NON");
        verify(span).event("checking.existence");
        verify(span).tag("vehicle.exists", "false");
        verify(span).error(any(EntityNotFoundException.class));
        verify(span).tag("exception", EntityNotFoundException.class.getName());
        verify(span).tag("business.error", "true");
        verify(spanInScope).close();
        verify(span).end();
    }

    @Test
    void updateBaseMsrpForMakeAndModel_ShouldReturnUpdatedCount() {
        // Arrange
        int expectedCount = 15;
        when(vehicleRepository.updateBaseMsrpForMakeAndModel(
                msrpRequestDTO.getMake(),
                msrpRequestDTO.getModel(),
                msrpRequestDTO.getNewBaseMSRP())).thenReturn(expectedCount);

        // Act
        int result = vehicleService.updateBaseMsrpForMakeAndModel(msrpRequestDTO);

        // Assert
        assertThat(result).isEqualTo(expectedCount);
        verify(vehicleRepository, times(1)).updateBaseMsrpForMakeAndModel(
                msrpRequestDTO.getMake(),
                msrpRequestDTO.getModel(),
                msrpRequestDTO.getNewBaseMSRP());

        // Verify tracing interactions
        verify(tracer).nextSpan();
        verify(span).name("ElectricVehicleService.updateBaseMsrpForMakeAndModel");
        verify(span).start();
        verify(span).tag("update.make", "TESLA");
        verify(span).tag("update.model", "Model 3");
        verify(span).tag("update.new_msrp", msrpRequestDTO.getNewBaseMSRP().toString());
        verify(span).event("repository.updateBaseMsrpForMakeAndModel");
        verify(span).tag("update.count", String.valueOf(expectedCount));
        verify(spanInScope).close();
        verify(span).end();
    }

    @Test
    void getAllVehicles_WhenNoVehiclesExist_ShouldReturnEmptyPage() {
        // Arrange
        Page<ElectricVehicle> emptyPage = new PageImpl<>(Collections.emptyList());
        Pageable pageable = PageRequest.of(0, 10);

        when(vehicleRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<ElectricVehicleDTO> result = vehicleService.getAllVehicles(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verify(vehicleRepository, times(1)).findAll(pageable);
        verify(vehicleMapper, never()).toDTO(any()); // Mapper should never be called with empty list

        // Verify tracing interactions
        verify(tracer).nextSpan();
        verify(span).name("ElectricVehicleService.getAllVehicles");
        verify(span).start();
        verify(span).tag("page.number", "0");
        verify(span).tag("page.size", "10");
        verify(span).tag(eq("page.sort"), anyString());
        verify(span).event("repository.findAll");
        verify(span).event("mapper.toDTO.page"); // Still triggered even if page is empty
        verify(spanInScope).close();
        verify(span).end();
    }

    @Test
    void updateVehicle_WithNullDtoVin_ShouldUsePathVin() {
        // Arrange
        String pathVin = "TEST123456";
        ElectricVehicleDTO updateDto = new ElectricVehicleDTO();
        updateDto.setVin(null); // Null VIN in DTO
        updateDto.setMake("TESLA");
        updateDto.setModel("Model 3 Refresh");
        updateDto.setDolVehicleId(123456789L); // Include DOL ID for tagging test

        ElectricVehicle existingEntity = new ElectricVehicle();
        existingEntity.setVin(pathVin);
        existingEntity.setMake("TESLA");
        existingEntity.setModel("Model 3");
        existingEntity.setDolVehicleId(123456789L); // Same DOL ID

        // Need to mock the updateEntityFromDto behavior as in updateVehicle_WhenVehicleExists
        doAnswer(invocation -> {
            ElectricVehicleDTO dto = invocation.getArgument(0);
            ElectricVehicle entity = invocation.getArgument(1);
            // Simulate updating the entity based on DTO (excluding VIN as it's null)
            entity.setMake(dto.getMake());
            entity.setModel(dto.getModel());
            // entity.setVin(dto.getVin()); // This line should NOT be here for this test scenario
            return null;
        }).when(vehicleMapper).updateEntityFromDto(updateDto, existingEntity);


        when(vehicleRepository.findById(pathVin)).thenReturn(Optional.of(existingEntity));
        when(vehicleRepository.save(existingEntity)).thenReturn(existingEntity); // Save returns the modified entity

        // --- FIX START ---
        // Create a separate DTO instance to represent the expected result
        ElectricVehicleDTO expectedResultDto = new ElectricVehicleDTO();
        expectedResultDto.setVin(pathVin); // The VIN should come from the path/entity, not the null DTO VIN
        expectedResultDto.setMake(updateDto.getMake()); // Populate with other expected updated fields
        expectedResultDto.setModel(updateDto.getModel());
        expectedResultDto.setDolVehicleId(updateDto.getDolVehicleId());
        expectedResultDto.setBaseMSRP(existingEntity.getBaseMSRP()); // Assuming MSRP is not updated by this DTO


        // Mock the mapper.toDTO to return this expected result DTO
        when(vehicleMapper.toDTO(existingEntity)).thenReturn(expectedResultDto);
        // --- FIX END ---


        // Act
        ElectricVehicleDTO result = vehicleService.updateVehicle(pathVin, updateDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getVin()).isEqualTo(pathVin); // Assert VIN from path is used/maintained
        assertThat(result.getModel()).isEqualTo("Model 3 Refresh"); // Assert other fields are updated

        verify(vehicleRepository, times(1)).findById(pathVin);
        verify(vehicleMapper, times(1)).updateEntityFromDto(updateDto, existingEntity);
        verify(vehicleRepository, times(1)).save(existingEntity);
        verify(vehicleMapper, times(1)).toDTO(existingEntity); // Verify mapping from the saved entity


        // Verify tracing interactions
        verify(tracer).nextSpan();
        verify(span).name("ElectricVehicleService.updateVehicle");
        verify(span).start();
        verify(span).tag("vin.prefix", "TES"); // Path VIN prefix
        verify(span).event("fetching.existing");
        verify(span).tag("vehicle.found", "true");
        verify(span).tag("vin.mismatch", "false"); // Null DTO VIN doesn't cause mismatch error
        verify(span).event("mapper.updateEntityFromDto");
        verify(span).event("repository.save");
        verify(span).tag("updated.dol.id.prefix", "123");
        verify(span).event("mapper.toDTO");
        verify(spanInScope).close();
        verify(span).end();
    }


    @Test
    void updateBaseMsrpForMakeAndModel_WithZeroUpdates_ShouldReturnZero() {
        // Arrange
        UpdateMsrpRequestDTO request = new UpdateMsrpRequestDTO();
        request.setMake("NONEXISTENT");
        request.setModel("Model");
        request.setNewBaseMSRP(BigDecimal.valueOf(50000));

        when(vehicleRepository.updateBaseMsrpForMakeAndModel(
                request.getMake(), request.getModel(), request.getNewBaseMSRP()
        )).thenReturn(0);

        // Act
        int result = vehicleService.updateBaseMsrpForMakeAndModel(request);

        // Assert
        assertThat(result).isZero();
        verify(vehicleRepository, times(1)).updateBaseMsrpForMakeAndModel(
                request.getMake(), request.getModel(), request.getNewBaseMSRP()
        );

        // Verify tracing interactions
        verify(tracer).nextSpan();
        verify(span).name("ElectricVehicleService.updateBaseMsrpForMakeAndModel");
        verify(span).start();
        verify(span).tag("update.make", "NONEXISTENT");
        verify(span).tag("update.model", "Model");
        verify(span).tag("update.new_msrp", request.getNewBaseMSRP().toString());
        verify(span).event("repository.updateBaseMsrpForMakeAndModel");
        verify(span).tag("update.count", "0");
        verify(spanInScope).close();
        verify(span).end();
    }

    @Test
    void createVehicle_WithNullInput_ShouldThrowNullPointerException() {
        // Act & Assert
        // This test verifies a basic null check before any significant logic is executed.
        // Tracing might not even start or would immediately fail.
        // We still include tracer mocking for consistency, but the NPE should happen first.
        assertThrows(NullPointerException.class, () -> vehicleService.createVehicle(null));

        // Verify that business logic was not executed
        verify(vehicleRepository, never()).existsById(any());
        verify(vehicleMapper, never()).toEntity(any());
        verify(vehicleRepository, never()).save(any());
    }
}