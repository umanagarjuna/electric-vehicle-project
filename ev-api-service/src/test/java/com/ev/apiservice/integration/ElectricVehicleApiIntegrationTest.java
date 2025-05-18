package com.ev.apiservice.integration;

import com.ev.apiservice.config.PostgresTestContainer;
import com.ev.apiservice.dto.CreateElectricVehicleDTO;
import com.ev.apiservice.dto.ElectricVehicleDTO;
import com.ev.apiservice.dto.PointDTO;
import com.ev.apiservice.dto.UpdateMsrpRequestDTO;
import com.ev.apiservice.model.ElectricVehicle;
import com.ev.apiservice.repository.ElectricVehicleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(PostgresTestContainer.class)
public class ElectricVehicleApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ElectricVehicleRepository vehicleRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private CreateElectricVehicleDTO createDTO;
    private ElectricVehicleDTO updateDTO;
    private UpdateMsrpRequestDTO msrpRequestDTO;

    @BeforeEach
    void setUp() {
        // Setup test DTOs
        createDTO = new CreateElectricVehicleDTO();
        createDTO.setVin("TESTAPI123"); // 10 char max
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

        updateDTO = new ElectricVehicleDTO();
        updateDTO.setVin("SAMPLE1234"); // 10 char max
        updateDTO.setMake("TESLA");
        updateDTO.setModel("Model 3 Performance");
        updateDTO.setModelYear(2023);
        updateDTO.setBaseMSRP(new BigDecimal("55000.00"));
        updateDTO.setDolVehicleId(123456789L);
        updateDTO.setCounty("King");
        updateDTO.setCity("Seattle");
        updateDTO.setState("WA");
        updateDTO.setPostalCode("98101");
        updateDTO.setElectricVehicleType("Battery Electric Vehicle (BEV)");
        updateDTO.setCafvEligibilityStatus("Clean Alternative Fuel Vehicle Eligible");
        updateDTO.setElectricRange(320);
        updateDTO.setLegislativeDistrict("43");
        updateDTO.setElectricUtility("SEATTLE CITY LIGHT");
        updateDTO.setCensusTract2020(53033005600L);
        updateDTO.setVehicleLocation(new PointDTO(-122.33207, 47.60611));

        msrpRequestDTO = new UpdateMsrpRequestDTO();
        msrpRequestDTO.setMake("TESLA");
        msrpRequestDTO.setModel("Model 3");
        msrpRequestDTO.setNewBaseMSRP(new BigDecimal("47000.00"));
    }

    @Test
    @Sql({"/sql/cleanup.sql", "/sql/sample-data.sql"})
    void getAllVehicles_ShouldReturnPaginatedVehicles() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "vin,asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.pageable.pageNumber", is(0)))
                .andExpect(jsonPath("$.pageable.pageSize", is(20)));
    }

    @Test
    @Sql({"/sql/cleanup.sql", "/sql/sample-data.sql"})
    void getVehicleByVin_WhenVehicleExists_ShouldReturnVehicle() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/{vin}", "SAMPLE1234"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.vin", is("SAMPLE1234")));
    }

    @Test
    void getVehicleByVin_WhenVehicleDoesNotExist_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/{vin}", "NONEXIST"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createVehicle_WithValidData_ShouldCreateAndReturnVehicle() throws Exception {
        String dtoJson = objectMapper.writeValueAsString(createDTO);

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.vin", is("TESTAPI123")))
                .andExpect(jsonPath("$.make", is("TESLA")))
                .andExpect(jsonPath("$.model", is("Model 3")));

        // Verify vehicle was persisted
        Optional<ElectricVehicle> savedVehicle = vehicleRepository.findById("TESTAPI123");
        assertThat(savedVehicle).isPresent();
        assertThat(savedVehicle.get().getMake()).isEqualTo("TESLA");
    }

    @Test
    void createVehicle_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Create an invalid DTO with null required fields
        CreateElectricVehicleDTO invalidDTO = new CreateElectricVehicleDTO();
        invalidDTO.setVin(""); // Empty VIN, should fail validation
        invalidDTO.setMake(null); // Null make, should fail validation
        invalidDTO.setModel("Model 3");
        invalidDTO.setDolVehicleId(null); // Null dolVehicleId, should fail validation

        String dtoJson = objectMapper.writeValueAsString(invalidDTO);

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Validation Failed")))
                .andExpect(jsonPath("$.fieldErrors", aMapWithSize(greaterThan(0))));
    }

    @Test
    @Sql({"/sql/cleanup.sql", "/sql/sample-data.sql"})
    void updateVehicle_WithValidData_ShouldUpdateAndReturnVehicle() throws Exception {
        String dtoJson = objectMapper.writeValueAsString(updateDTO);

        mockMvc.perform(put("/api/v1/vehicles/{vin}", "SAMPLE1234")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.vin", is("SAMPLE1234")))
                .andExpect(jsonPath("$.model", is("Model 3 Performance")));

        // Verify vehicle was updated
        Optional<ElectricVehicle> updatedVehicle = vehicleRepository.findById("SAMPLE1234");
        assertThat(updatedVehicle).isPresent();
        assertThat(updatedVehicle.get().getModel()).isEqualTo("Model 3 Performance");
    }

    @Test
    void updateVehicle_WithMismatchedVins_ShouldReturnBadRequest() throws Exception {
        // The DTO contains a VIN that doesn't match the path parameter
        updateDTO.setVin("DIFF12345");
        String dtoJson = objectMapper.writeValueAsString(updateDTO);

        mockMvc.perform(put("/api/v1/vehicles/{vin}", "SAMPLE1234")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Path VIN")));
    }

    @Test
    @Sql({"/sql/cleanup.sql", "/sql/sample-data.sql"})
    void deleteVehicle_WhenVehicleExists_ShouldDeleteVehicle() throws Exception {
        // Verify vehicle exists before deletion
        assertThat(vehicleRepository.existsById("SAMPLE1234")).isTrue();

        mockMvc.perform(delete("/api/v1/vehicles/{vin}", "SAMPLE1234"))
                .andExpect(status().isNoContent());

        // Verify vehicle was deleted
        assertThat(vehicleRepository.existsById("SAMPLE1234")).isFalse();
    }

    @Test
    void deleteVehicle_WhenVehicleDoesNotExist_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/vehicles/{vin}", "NONEXIST"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql({"/sql/cleanup.sql", "/sql/sample-data.sql"})
    void updateBaseMsrpForMakeAndModel_ShouldUpdateAndReturnCount() throws Exception {
        String dtoJson = objectMapper.writeValueAsString(msrpRequestDTO);

        MvcResult result = mockMvc.perform(patch("/api/v1/vehicles/batch/msrp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("Base MSRP updated")))
                .andExpect(jsonPath("$.make", is("TESLA")))
                .andExpect(jsonPath("$.model", is("Model 3")))
                .andExpect(jsonPath("$.updatedCount", greaterThanOrEqualTo(0)))
                .andReturn();

        // Get the updated count from the response
        String responseContent = result.getResponse().getContentAsString();
        int updatedCount = objectMapper.readTree(responseContent).get("updatedCount").asInt();

        // If any vehicles were updated, verify they have the new MSRP
        if (updatedCount > 0) {
            List<ElectricVehicle> updatedVehicles = vehicleRepository
                    .findByMakeIgnoreCaseAndModelIgnoreCase("TESLA", "Model 3");

            assertThat(updatedVehicles).isNotEmpty();
            assertThat(updatedVehicles).allMatch(vehicle ->
                    vehicle.getBaseMSRP().compareTo(msrpRequestDTO.getNewBaseMSRP()) == 0);
        }
    }
}