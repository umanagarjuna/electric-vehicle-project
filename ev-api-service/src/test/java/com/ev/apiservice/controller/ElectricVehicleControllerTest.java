package com.ev.apiservice.controller;

import com.ev.apiservice.dto.CreateElectricVehicleDTO;
import com.ev.apiservice.dto.ElectricVehicleDTO;
import com.ev.apiservice.dto.PointDTO;
import com.ev.apiservice.dto.UpdateMsrpRequestDTO;
import com.ev.apiservice.service.ElectricVehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ElectricVehicleControllerTest {

    @Mock
    private ElectricVehicleService vehicleService;

    @InjectMocks
    private ElectricVehicleController controller;

    private ElectricVehicleDTO sampleVehicleDTO;
    private CreateElectricVehicleDTO createDTO;
    private UpdateMsrpRequestDTO msrpRequestDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Setup test data
        sampleVehicleDTO = new ElectricVehicleDTO();
        sampleVehicleDTO.setVin("TEST123456");
        sampleVehicleDTO.setMake("TESLA");
        sampleVehicleDTO.setModel("Model 3");
        sampleVehicleDTO.setModelYear(2023);
        sampleVehicleDTO.setBaseMSRP(new BigDecimal("45000.00"));
        sampleVehicleDTO.setDolVehicleId(123456789L);
        sampleVehicleDTO.setVehicleLocation(new PointDTO(-122.33207, 47.60611));

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
    void getAllVehicles_ShouldReturnPageOfVehicles() {
        // Arrange
        Page<ElectricVehicleDTO> vehiclePage = new PageImpl<>(List.of(sampleVehicleDTO));
        when(vehicleService.getAllVehicles(any(Pageable.class))).thenReturn(vehiclePage);

        // Act
        ResponseEntity<Page<ElectricVehicleDTO>> responseEntity = controller.getAllVehicles(pageable);

        // Assert
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getContent()).hasSize(1);
        assertThat(responseEntity.getBody().getContent().get(0).getVin()).isEqualTo("TEST123456");
        verify(vehicleService, times(1)).getAllVehicles(pageable);
    }

    @Test
    void getVehicleByVin_WhenVehicleExists_ShouldReturnVehicleDTO() {
        // Arrange
        String vin = "TEST123456";
        when(vehicleService.getVehicleByVin(vin)).thenReturn(Optional.of(sampleVehicleDTO));

        // Act
        ResponseEntity<ElectricVehicleDTO> responseEntity = controller.getVehicleByVin(vin);

        // Assert
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getVin()).isEqualTo(vin);
        verify(vehicleService, times(1)).getVehicleByVin(vin);
    }

    @Test
    void getVehicleByVin_WhenVehicleDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        String vin = "NONEXISTENT";
        when(vehicleService.getVehicleByVin(vin)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ElectricVehicleDTO> responseEntity = controller.getVehicleByVin(vin);

        // Assert
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNull();
        verify(vehicleService, times(1)).getVehicleByVin(vin);
    }

    @Test
    void createVehicle_ShouldReturnCreatedVehicle() {
        // Arrange
        when(vehicleService.createVehicle(any(CreateElectricVehicleDTO.class))).thenReturn(sampleVehicleDTO);

        // Act
        ResponseEntity<ElectricVehicleDTO> responseEntity = controller.createVehicle(createDTO);

        // Assert
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getVin()).isEqualTo("TEST123456");
        verify(vehicleService, times(1)).createVehicle(createDTO);
    }

    @Test
    void updateVehicle_WhenVinMatches_ShouldReturnUpdatedVehicle() {
        // Arrange
        String vin = "TEST123456";
        ElectricVehicleDTO updateDTO = new ElectricVehicleDTO();
        updateDTO.setVin(vin);
        updateDTO.setMake("TESLA");
        updateDTO.setModel("Model 3 Performance");
        updateDTO.setModelYear(2023);
        updateDTO.setDolVehicleId(123456789L);

        when(vehicleService.updateVehicle(eq(vin), any(ElectricVehicleDTO.class))).thenReturn(updateDTO);

        // Act
        ResponseEntity<ElectricVehicleDTO> responseEntity = controller.updateVehicle(vin, updateDTO);

        // Assert
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getVin()).isEqualTo(vin);
        assertThat(responseEntity.getBody().getModel()).isEqualTo("Model 3 Performance");
        verify(vehicleService, times(1)).updateVehicle(eq(vin), any(ElectricVehicleDTO.class));
    }

    @Test
    void updateVehicle_WhenNullVinInDTO_ShouldSetVinFromPath() {
        // Arrange
        String vin = "TEST123456";
        ElectricVehicleDTO updateDTO = new ElectricVehicleDTO();
        updateDTO.setVin(null); // Null VIN in DTO
        updateDTO.setMake("TESLA");
        updateDTO.setModel("Model 3 Performance");
        updateDTO.setModelYear(2023);
        updateDTO.setDolVehicleId(123456789L);

        ElectricVehicleDTO expectedResponseDTO = new ElectricVehicleDTO();
        expectedResponseDTO.setVin(vin);
        expectedResponseDTO.setMake("TESLA");
        expectedResponseDTO.setModel("Model 3 Performance");
        expectedResponseDTO.setModelYear(2023);
        expectedResponseDTO.setDolVehicleId(123456789L);

        when(vehicleService.updateVehicle(eq(vin), any(ElectricVehicleDTO.class))).thenReturn(expectedResponseDTO);

        // Act
        ResponseEntity<ElectricVehicleDTO> responseEntity = controller.updateVehicle(vin, updateDTO);

        // Assert
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getVin()).isEqualTo(vin);
        verify(vehicleService, times(1)).updateVehicle(eq(vin), any(ElectricVehicleDTO.class));

        // Verify the DTO passed to the service has the VIN from the path
        assertThat(updateDTO.getVin()).isEqualTo(vin);
    }

    @Test
    void deleteVehicle_ShouldReturnNoContent() {
        // Arrange
        String vin = "TEST123456";
        doNothing().when(vehicleService).deleteVehicle(vin);

        // Act
        ResponseEntity<Void> responseEntity = controller.deleteVehicle(vin);

        // Assert
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(responseEntity.getBody()).isNull();
        verify(vehicleService, times(1)).deleteVehicle(vin);
    }

    @Test
    void updateBaseMsrpForMakeAndModel_ShouldReturnUpdatedCount() {
        // Arrange
        int updatedCount = 15;
        when(vehicleService.updateBaseMsrpForMakeAndModel(any(UpdateMsrpRequestDTO.class))).thenReturn(updatedCount);

        // Act
        ResponseEntity<Map<String, Object>> responseEntity = controller.updateBaseMsrpForMakeAndModel(msrpRequestDTO);

        // Assert
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().get("updatedCount")).isEqualTo(updatedCount);
        assertThat(responseEntity.getBody().get("make")).isEqualTo("TESLA");
        assertThat(responseEntity.getBody().get("model")).isEqualTo("Model 3");
        verify(vehicleService, times(1)).updateBaseMsrpForMakeAndModel(msrpRequestDTO);
    }

    @Test
    void updateVehicle_WhenDtoVinDoesNotMatchPathVin_ShouldThrowException() {
        // Arrange
        String pathVin = "PATH123456";
        ElectricVehicleDTO updateDTO = new ElectricVehicleDTO();
        updateDTO.setVin("DTO7890123"); // Different from path VIN

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.updateVehicle(pathVin, updateDTO)
        );

        assertThat(exception.getMessage()).contains("Path VIN");
        assertThat(exception.getMessage()).contains("must match DTO VIN");

        // Verify service was never called
        verify(vehicleService, never()).updateVehicle(any(), any());
    }

    @Test
    void updateBaseMsrpForMakeAndModel_WhenNoRecordsUpdated_ShouldReturnZeroCount() {
        // Arrange
        UpdateMsrpRequestDTO msrpRequest = new UpdateMsrpRequestDTO();
        msrpRequest.setMake("NONEXISTENT");
        msrpRequest.setModel("Model");
        msrpRequest.setNewBaseMSRP(BigDecimal.valueOf(50000));

        when(vehicleService.updateBaseMsrpForMakeAndModel(any(UpdateMsrpRequestDTO.class))).thenReturn(0);

        // Act
        ResponseEntity<Map<String, Object>> responseEntity = controller.updateBaseMsrpForMakeAndModel(msrpRequest);

        // Assert
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().get("updatedCount")).isEqualTo(0);
        assertThat(responseEntity.getBody().get("make")).isEqualTo("NONEXISTENT");
        assertThat(responseEntity.getBody().get("model")).isEqualTo("Model");
        verify(vehicleService, times(1)).updateBaseMsrpForMakeAndModel(msrpRequest);
    }

    @Test
    void createVehicle_WithNull_ShouldHandleNullPointerGracefully() {
        // This test ensures that the controller handles null input gracefully

        // Act & Assert - Just check that a NullPointerException is thrown directly
        // rather than some other unexpected exception
        assertThrows(NullPointerException.class, () -> controller.createVehicle(null));

        // Verify service was never called with null
        verify(vehicleService, never()).createVehicle(any());
    }
}
