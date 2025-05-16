package com.ev.dataloaderjava.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j // Lombok for logging
public class CsvDataLoaderService {

    private final JdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;

    @Value("${data.loader.csv.filepath}") // Ensure this property is in your application.properties
    private String csvFilePath;

    @Value("${data.loader.batch.size:1000}")
    private int batchSize;

    // CSV Header Names Constants
    public static final String HEADER_VIN = "VIN (1-10)";
    public static final String HEADER_COUNTY = "County";
    public static final String HEADER_CITY = "City";
    public static final String HEADER_STATE = "State";
    public static final String HEADER_POSTAL_CODE = "Postal Code";
    public static final String HEADER_MODEL_YEAR = "Model Year";
    public static final String HEADER_MAKE = "Make";
    public static final String HEADER_MODEL = "Model";
    public static final String HEADER_EV_TYPE = "Electric Vehicle Type";
    public static final String HEADER_CAFV_ELIGIBILITY = "Clean Alternative Fuel Vehicle (CAFV) Eligibility";
    public static final String HEADER_ELECTRIC_RANGE = "Electric Range";
    public static final String HEADER_BASE_MSRP = "Base MSRP";
    public static final String HEADER_LEGISLATIVE_DISTRICT = "Legislative District";
    public static final String HEADER_DOL_VEHICLE_ID = "DOL Vehicle ID";
    public static final String HEADER_VEHICLE_LOCATION = "Vehicle Location";
    public static final String HEADER_ELECTRIC_UTILITY = "Electric Utility";
    public static final String HEADER_CENSUS_TRACT_2020 = "2020 Census Tract";


    @Autowired
    public CsvDataLoaderService(JdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
    }

    @Transactional // Perform the entire load operation in a single transaction
    public void loadData() throws Exception { // Consider more specific exceptions or runtime exceptions
        log.info("Starting data loading (UPSERT mode) from CSV: {}", csvFilePath);
        Resource resource = resourceLoader.getResource(csvFilePath);
        if (!resource.exists()) {
            log.error("CSV file not found at path: {}", csvFilePath);
            throw new RuntimeException("CSV file not found: " + csvFilePath);
        }

        List<CSVRecord> allRecords = new ArrayList<>();
        try (Reader reader = new InputStreamReader(resource.getInputStream());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                     .setHeader() // Automatically uses the first record as header names
                     .setSkipHeaderRecord(true) // Skips the header row from data processing
                     .setIgnoreHeaderCase(true) // Ignores case for header names
                     .setTrim(true)             // Trims whitespace from values
                     .build())) {
            csvParser.forEach(allRecords::add);
        }

        log.info("Total records read from CSV (excluding header): {}", allRecords.size());
        if (allRecords.isEmpty()) {
            log.info("No records to load.");
            return;
        }

        String sql = """
            INSERT INTO electric_vehicle_population (
                vin, county, city, state, postal_code, model_year, make, model,
                electric_vehicle_type, cafv_eligibility_status, electric_range, base_msrp,
                legislative_district, dol_vehicle_id, vehicle_location_point,
                electric_utility, census_tract_2020
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ST_GeomFromText(?, 4326), ?, ?)
            ON CONFLICT (vin) DO UPDATE SET
                county = EXCLUDED.county,
                city = EXCLUDED.city,
                state = EXCLUDED.state,
                postal_code = EXCLUDED.postal_code,
                model_year = EXCLUDED.model_year,
                make = EXCLUDED.make,
                model = EXCLUDED.model,
                electric_vehicle_type = EXCLUDED.electric_vehicle_type,
                cafv_eligibility_status = EXCLUDED.cafv_eligibility_status,
                electric_range = EXCLUDED.electric_range,
                base_msrp = EXCLUDED.base_msrp,
                legislative_district = EXCLUDED.legislative_district,
                dol_vehicle_id = EXCLUDED.dol_vehicle_id,
                vehicle_location_point = EXCLUDED.vehicle_location_point,
                electric_utility = EXCLUDED.electric_utility,
                census_tract_2020 = EXCLUDED.census_tract_2020
            """;

        int totalRecordsProcessed = 0;
        for (int i = 0; i < allRecords.size(); i += batchSize) {
            final List<CSVRecord> batchList = allRecords.subList(i, Math.min(i + batchSize, allRecords.size()));

            log.debug("Processing batch starting at index {}. Batch size: {}", i, batchList.size());

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int k) throws SQLException {
                    CSVRecord record = batchList.get(k);

                    ps.setString(1, record.get(HEADER_VIN));
                    ps.setString(2, record.get(HEADER_COUNTY));
                    ps.setString(3, record.get(HEADER_CITY));
                    ps.setString(4, record.get(HEADER_STATE));
                    ps.setString(5, record.get(HEADER_POSTAL_CODE)); // Assumes Postal Code is text

                    setInteger(ps, 6, record.get(HEADER_MODEL_YEAR), HEADER_MODEL_YEAR, record.get(HEADER_VIN));
                    ps.setString(7, record.get(HEADER_MAKE));
                    ps.setString(8, record.get(HEADER_MODEL));
                    ps.setString(9, record.get(HEADER_EV_TYPE));
                    ps.setString(10, record.get(HEADER_CAFV_ELIGIBILITY));
                    setInteger(ps, 11, record.get(HEADER_ELECTRIC_RANGE), HEADER_ELECTRIC_RANGE, record.get(HEADER_VIN));
                    setBigDecimal(ps, 12, record.get(HEADER_BASE_MSRP), HEADER_BASE_MSRP, record.get(HEADER_VIN));
                    ps.setString(13, record.get(HEADER_LEGISLATIVE_DISTRICT));

                    // DOL Vehicle ID is NOT NULL in your schema.
                    // The setLong helper will attempt to set NULL if parsing fails or if empty.
                    // This will lead to a database constraint violation if the value is indeed missing/invalid.
                    // Consider how you want to handle this (e.g., skip record, fail transaction).
                    // For now, it relies on the database to enforce the NOT NULL constraint.
                    setLong(ps, 14, record.get(HEADER_DOL_VEHICLE_ID), HEADER_DOL_VEHICLE_ID, record.get(HEADER_VIN));

                    String locationWkt = record.get(HEADER_VEHICLE_LOCATION);
                    if (isValidWktPoint(locationWkt)) {
                        ps.setString(15, locationWkt);
                    } else {
                        log.warn("Invalid or empty Vehicle Location for VIN {}: '{}'. Setting related geometry to NULL.", record.get(HEADER_VIN), locationWkt);
                        ps.setNull(15, Types.VARCHAR); // ST_GeomFromText(NULL, ...) will result in NULL geometry
                    }

                    ps.setString(16, record.get(HEADER_ELECTRIC_UTILITY));
                    setLong(ps, 17, record.get(HEADER_CENSUS_TRACT_2020), HEADER_CENSUS_TRACT_2020, record.get(HEADER_VIN));
                }

                @Override
                public int getBatchSize() {
                    return batchList.size();
                }
            });
            totalRecordsProcessed += batchList.size();
            log.info("Processed batch. Total records considered for UPSERT so far: {}", totalRecordsProcessed);
        }
        log.info("Data loading (UPSERT mode) completed. Total records from CSV processed: {}", allRecords.size());
    }

    private void setInteger(PreparedStatement ps, int index, String value, String fieldName, String vin) throws SQLException {
        try {
            if (value != null && !value.trim().isEmpty()) {
                ps.setInt(index, Integer.parseInt(value.trim()));
            } else {
                ps.setNull(index, Types.INTEGER);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid integer value for {} for VIN {}: '{}'. Setting to NULL.", fieldName, vin, value);
            ps.setNull(index, Types.INTEGER);
        }
    }

    private void setLong(PreparedStatement ps, int index, String value, String fieldName, String vin) throws SQLException {
        try {
            if (value != null && !value.trim().isEmpty()) {
                ps.setLong(index, Long.parseLong(value.trim()));
            } else {
                // If the column is NOT NULL in the DB, this will cause an error at DB level, which is often desired.
                ps.setNull(index, Types.BIGINT);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid long value for {} for VIN {}: '{}'. Setting to NULL.", fieldName, vin, value);
            ps.setNull(index, Types.BIGINT);
        }
    }

    private void setBigDecimal(PreparedStatement ps, int index, String value, String fieldName, String vin) throws SQLException {
        try {
            if (value != null && !value.trim().isEmpty()) {
                ps.setBigDecimal(index, new BigDecimal(value.trim()));
            } else {
                ps.setNull(index, Types.NUMERIC);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid BigDecimal value for {} for VIN {}: '{}'. Setting to NULL.", fieldName, vin, value);
            ps.setNull(index, Types.NUMERIC);
        }
    }

    private boolean isValidWktPoint(String wkt) {
        // Basic check, can be enhanced for more strict WKT validation if needed
        return wkt != null && !wkt.trim().isEmpty() && wkt.toUpperCase().startsWith("POINT (") && wkt.endsWith(")");
    }
}