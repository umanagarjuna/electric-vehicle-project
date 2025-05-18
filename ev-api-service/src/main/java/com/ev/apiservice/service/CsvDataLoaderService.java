package com.ev.apiservice.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for loading electric vehicle data from CSV files into the database.
 */
@Service
@Slf4j
public class CsvDataLoaderService {

    private final JdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;
    private final TransactionTemplate transactionTemplate;

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
    public CsvDataLoaderService(JdbcTemplate jdbcTemplate,
                                ResourceLoader resourceLoader,
                                PlatformTransactionManager transactionManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * Counts the number of records in a CSV file.
     *
     * @param csvFilePath Path to the CSV file
     * @return The number of records in the file
     * @throws RuntimeException if the file cannot be read
     */
    public int countRecords(String csvFilePath) throws RuntimeException {
        try {
            Reader reader = getReader(csvFilePath);
            try (reader;
                 CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                         .setHeader()
                         .setSkipHeaderRecord(true)
                         .setIgnoreHeaderCase(true)
                         .setTrim(true)
                         .build())) {
                return (int) csvParser.stream().count();
            }
        } catch (Exception e) {
            log.error("Error counting CSV records: {}", csvFilePath, e);
            throw new RuntimeException("Error counting CSV records: " + e.getMessage(), e);
        }
    }

    /**
     * Loads electric vehicle data from a CSV file into the database.
     * Legacy method without progress tracking.
     *
     * @param csvFilePath Path to the CSV file (can be a file path or classpath resource)
     * @param batchSize Number of records to process in each batch
     * @return The number of records processed
     * @throws RuntimeException if the file cannot be read or processed
     */
    public int loadData(String csvFilePath, int batchSize) throws RuntimeException {
        // Simply delegate to the new method with null callback
        return loadData(csvFilePath, batchSize, null);
    }

    /**
     * Loads electric vehicle data from a CSV file into the database with progress reporting.
     * Each batch is processed in its own transaction to prevent connection timeouts.
     *
     * @param csvFilePath Path to the CSV file
     * @param batchSize Number of records to process in each batch
     * @param progressCallback Callback for reporting progress
     * @return The number of records processed
     * @throws RuntimeException if the file cannot be read or processed
     */
    @Transactional(propagation = Propagation.NEVER) // Ensure no outer transaction
    public int loadData(String csvFilePath, int batchSize, ProgressCallback progressCallback) throws RuntimeException {
        log.info("Starting data loading (UPSERT mode) from CSV: {}", csvFilePath);

        List<CSVRecord> allRecords = new ArrayList<>();

        try {
            Reader reader = getReader(csvFilePath);
            try (reader;
                 CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                         .setHeader()
                         .setSkipHeaderRecord(true)
                         .setIgnoreHeaderCase(true)
                         .setTrim(true)
                         .build())) {
                csvParser.forEach(allRecords::add);
            }
        } catch (Exception e) {
            log.error("Error reading CSV file: {}", csvFilePath, e);
            throw new RuntimeException("Error reading CSV file: " + e.getMessage(), e);
        }

        log.info("Total records read from CSV (excluding header): {}", allRecords.size());
        if (allRecords.isEmpty()) {
            log.info("No records to load.");
            return 0;
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
        try {
            for (int i = 0; i < allRecords.size(); i += batchSize) {
                final int startIndex = i;
                final int endIndex = Math.min(i + batchSize, allRecords.size());
                final List<CSVRecord> batchList = allRecords.subList(startIndex, endIndex);

                log.debug("Processing batch starting at index {}. Batch size: {}", startIndex, batchList.size());

                // Process each batch in its own transaction
                transactionTemplate.execute(status -> {
                    jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int k) throws SQLException {
                            CSVRecord record = batchList.get(k);

                            ps.setString(1, record.get(HEADER_VIN));
                            ps.setString(2, record.get(HEADER_COUNTY));
                            ps.setString(3, record.get(HEADER_CITY));
                            ps.setString(4, record.get(HEADER_STATE));
                            ps.setString(5, record.get(HEADER_POSTAL_CODE));

                            setInteger(ps, 6, record.get(HEADER_MODEL_YEAR), HEADER_MODEL_YEAR, record.get(HEADER_VIN));
                            ps.setString(7, record.get(HEADER_MAKE));
                            ps.setString(8, record.get(HEADER_MODEL));
                            ps.setString(9, record.get(HEADER_EV_TYPE));
                            ps.setString(10, record.get(HEADER_CAFV_ELIGIBILITY));
                            setInteger(ps, 11, record.get(HEADER_ELECTRIC_RANGE), HEADER_ELECTRIC_RANGE, record.get(HEADER_VIN));
                            setBigDecimal(ps, 12, record.get(HEADER_BASE_MSRP), HEADER_BASE_MSRP, record.get(HEADER_VIN));
                            ps.setString(13, record.get(HEADER_LEGISLATIVE_DISTRICT));
                            setLong(ps, 14, record.get(HEADER_DOL_VEHICLE_ID), HEADER_DOL_VEHICLE_ID, record.get(HEADER_VIN));

                            String locationWkt = record.get(HEADER_VEHICLE_LOCATION);
                            if (isValidWktPoint(locationWkt)) {
                                ps.setString(15, locationWkt);
                            } else {
                                log.warn("Invalid or empty Vehicle Location for VIN {}: '{}'. Setting related geometry to NULL.",
                                        record.get(HEADER_VIN), locationWkt);
                                ps.setNull(15, Types.VARCHAR);
                            }

                            ps.setString(16, record.get(HEADER_ELECTRIC_UTILITY));
                            setLong(ps, 17, record.get(HEADER_CENSUS_TRACT_2020), HEADER_CENSUS_TRACT_2020, record.get(HEADER_VIN));
                        }

                        @Override
                        public int getBatchSize() {
                            return batchList.size();
                        }
                    });
                    return null; // Must return something for the execute() method
                });

                totalRecordsProcessed += batchList.size();
                log.info("Processed batch. Total records considered for UPSERT so far: {}", totalRecordsProcessed);

                // Report progress outside the transaction
                if (progressCallback != null) {
                    progressCallback.onProgress(totalRecordsProcessed);
                }
            }
        } catch (Exception e) {
            log.error("Error during data loading: {}", e.getMessage(), e);
            throw new RuntimeException("Error during data loading: " + e.getMessage(), e);
        }

        log.info("Data loading (UPSERT mode) completed. Total records from CSV processed: {}", allRecords.size());
        return totalRecordsProcessed;
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
        return wkt != null && !wkt.trim().isEmpty() && wkt.toUpperCase().startsWith("POINT (") && wkt.endsWith(")");
    }

    /**
     * Helper method to get a reader for a file.
     */
    private Reader getReader(String csvFilePath) throws Exception {
        if (csvFilePath.startsWith("classpath:")) {
            // Handle classpath resources
            Resource resource = resourceLoader.getResource(csvFilePath);
            if (!resource.exists()) {
                log.error("CSV file not found at classpath: {}", csvFilePath);
                throw new RuntimeException("CSV file not found: " + csvFilePath);
            }
            return new InputStreamReader(resource.getInputStream());
        } else if (csvFilePath.startsWith("file:")) {
            // Handle absolute file paths
            String path = csvFilePath.substring(5); // Remove "file:" prefix
            File file = new File(path);
            if (!file.exists() || !file.canRead()) {
                log.error("CSV file not found or not readable at path: {}", path);
                throw new RuntimeException("CSV file not found or not readable: " + path);
            }
            return new FileReader(file);
        } else {
            // Try as a regular resource
            Resource resource = resourceLoader.getResource(csvFilePath);
            if (resource.exists()) {
                return new InputStreamReader(resource.getInputStream());
            } else {
                // Try as a regular file
                File file = new File(csvFilePath);
                if (!file.exists() || !file.canRead()) {
                    log.error("CSV file not found or not readable at path: {}", csvFilePath);
                    throw new RuntimeException("CSV file not found or not readable: " + csvFilePath);
                }
                return new FileReader(file);
            }
        }
    }

    /**
     * Interface for progress tracking.
     */
    public interface ProgressCallback {
        void onProgress(int processedRecords);
    }
}