package com.ev.apiservice.service;

import com.ev.apiservice.dto.CreateElectricVehicleDTO;
import com.ev.apiservice.dto.ElectricVehicleDTO;
import com.ev.apiservice.dto.PointDTO;
import com.ev.apiservice.dto.UpdateMsrpRequestDTO;
import com.ev.apiservice.mapper.ElectricVehicleMapper;
import com.ev.apiservice.model.ElectricVehicle;
import com.ev.apiservice.repository.ElectricVehicleRepository;
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

import java.math.BigDecimal;
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
    }

    @Test
    void createVehicle_WhenVehicleExists_ShouldThrowIllegalArgumentException() {
        // Arrange
        when(vehicleRepository.existsById(createDTO.getVin())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> vehicleService.createVehicle(createDTO));
        verify(vehicleRepository, times(1)).existsById(createDTO.getVin());
        verify(vehicleMapper, never()).toEntity(any());
        verify(vehicleRepository, never()).save(any());
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
        updateDTO.setDolVehicleId(123456789L);

        when(vehicleRepository.findById(vin)).thenReturn(Optional.of(sampleEntity));
        doNothing().when(vehicleMapper).updateEntityFromDto(updateDTO, sampleEntity);
        when(vehicleRepository.save(sampleEntity)).thenReturn(sampleEntity);
        when(vehicleMapper.toDTO(sampleEntity)).thenReturn(updateDTO);

        // Act
        ElectricVehicleDTO result = vehicleService.updateVehicle(vin, updateDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getVin()).isEqualTo(vin);
        verify(vehicleRepository, times(1)).findById(vin);
        verify(vehicleMapper, times(1)).updateEntityFromDto(updateDTO, sampleEntity);
        verify(vehicleRepository, times(1)).save(sampleEntity);
        verify(vehicleMapper, times(1)).toDTO(sampleEntity);
    }

    @Test
    void updateVehicle_WhenVehicleDoesNotExist_ShouldThrowEntityNotFoundException() {
        // Arrange
        String vin = "NONEXISTENT";
        ElectricVehicleDTO updateDTO = new ElectricVehicleDTO();
        updateDTO.setVin(vin);

        when(vehicleRepository.findById(vin)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> vehicleService.updateVehicle(vin, updateDTO));
        verify(vehicleRepository, times(1)).findById(vin);
        verify(vehicleMapper, never()).updateEntityFromDto(any(), any());
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void updateVehicle_WhenVinMismatch_ShouldThrowIllegalArgumentException() {
        // Arrange
        String pathVin = "TEST123456";
        String dtoVin = "DIFFERENT789";
        ElectricVehicleDTO updateDTO = new ElectricVehicleDTO();
        updateDTO.setVin(dtoVin);

        when(vehicleRepository.findById(pathVin)).thenReturn(Optional.of(sampleEntity));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> vehicleService.updateVehicle(pathVin, updateDTO));
        verify(vehicleRepository, times(1)).findById(pathVin);
        verify(vehicleMapper, never()).updateEntityFromDto(any(), any());
        verify(vehicleRepository, never()).save(any());
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
    }

    @Test
    void deleteVehicle_WhenVehicleDoesNotExist_ShouldThrowEntityNotFoundException() {
        // Arrange
        String vin = "NONEXISTENT";
        when(vehicleRepository.existsById(vin)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> vehicleService.deleteVehicle(vin));
        verify(vehicleRepository, times(1)).existsById(vin);
        verify(vehicleRepository, never()).deleteById(any());
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
    }
}