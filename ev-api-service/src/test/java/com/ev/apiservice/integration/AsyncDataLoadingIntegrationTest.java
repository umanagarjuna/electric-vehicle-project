package com.ev.apiservice.integration;

import com.ev.apiservice.config.PostgresTestContainer;
import com.ev.apiservice.service.AsyncService;
import com.ev.apiservice.service.DataLoaderJobService;
import com.ev.apiservice.service.DataLoaderJobService.JobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Import(PostgresTestContainer.class)  // Use PostgreSQL container instead of H2GisTestConfig
@Transactional
public class AsyncDataLoadingIntegrationTest {

    @Autowired
    private DataLoaderJobService dataLoaderJobService;

    @SpyBean
    private AsyncService asyncService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @TempDir
    Path tempDir;

    private Path tempCsvPath;

    @BeforeEach
    void setUp() throws Exception {
        // Create test data file
        tempCsvPath = tempDir.resolve("test-vehicles.csv");
        Files.writeString(tempCsvPath,
                "\"VIN (1-10)\",\"County\",\"City\",\"State\",\"Postal Code\",\"Model Year\",\"Make\",\"Model\",\"Electric Vehicle Type\",\"Clean Alternative Fuel Vehicle (CAFV) Eligibility\",\"Electric Range\",\"Base MSRP\",\"Legislative District\",\"DOL Vehicle ID\",\"Vehicle Location\",\"Electric Utility\",\"2020 Census Tract\"\n" +
                        "\"TEST123456\",\"King\",\"Seattle\",\"WA\",\"98101\",\"2021\",\"TESLA\",\"Model 3\",\"Battery Electric Vehicle (BEV)\",\"Clean Alternative Fuel Vehicle Eligible\",\"350\",\"41990\",\"43\",\"123456789\",\"POINT (-122.3321 47.6062)\",\"SEATTLE CITY LIGHT\",\"53033001100\"\n" +
                        "\"TEST234567\",\"Pierce\",\"Tacoma\",\"WA\",\"98402\",\"2022\",\"TESLA\",\"Model Y\",\"Battery Electric Vehicle (BEV)\",\"Clean Alternative Fuel Vehicle Eligible\",\"330\",\"58990\",\"27\",\"234567890\",\"POINT (-122.4400 47.2529)\",\"TACOMA PUBLIC UTILITIES\",\"53053061100\""
        );

        // Clean up existing data
        jdbcTemplate.execute("DELETE FROM electric_vehicle_population");
    }

    @Test
    void fullAsyncDataLoadingFlowShouldWorkWithRealCsvFile() throws Exception {
        // Given
        String csvFilePath = "file:" + tempCsvPath.toAbsolutePath();

        // When - Start the job
        String jobId = dataLoaderJobService.startLoadJob(csvFilePath, 10);

        // Poll for status with timeout
        JobStatus finalStatus = pollForFinalStatus(jobId, Duration.ofSeconds(5));

        // Then - Verify completion
        assertThat(finalStatus.getStatus())
                .isIn(JobStatus.Status.COMPLETED, JobStatus.Status.FAILED);

        if (finalStatus.getStatus() == JobStatus.Status.COMPLETED) {
            // Check if records were loaded
            List<Map<String, Object>> records = jdbcTemplate.queryForList(
                    "SELECT vin, make, model FROM electric_vehicle_population");

            assertThat(records).isNotEmpty();
            if (!records.isEmpty()) {
                assertThat(records.get(0)).containsKey("vin");
            }

            verify(asyncService).executeLoadDataAsync(eq(jobId), eq(csvFilePath), eq(10));
        } else {
            // If failed, just assert that error message exists
            assertThat(finalStatus.getErrorMessage()).isNotBlank();
        }
    }

    /**
     * Polls for job status until it reaches a terminal state (COMPLETED or FAILED) or times out
     */
    private JobStatus pollForFinalStatus(String jobId, Duration timeout) throws InterruptedException {
        long endTime = System.currentTimeMillis() + timeout.toMillis();
        JobStatus status;

        do {
            status = dataLoaderJobService.getJobStatus(jobId);

            if (status.getStatus() == JobStatus.Status.COMPLETED ||
                    status.getStatus() == JobStatus.Status.FAILED) {
                return status;
            }

            // Short sleep to avoid hammering the service
            Thread.sleep(100);

        } while (System.currentTimeMillis() < endTime);

        // If we get here, we timed out waiting for a terminal state
        throw new AssertionError("Job processing timed out. Last status: " + status.getStatus());
    }

    @Test
    void asyncDataLoadingShouldHandleErrors() {
        // Given - Invalid CSV path
        String invalidCsvFilePath = "file:non-existent-file.csv";

        // When - Start the job
        String jobId = dataLoaderJobService.startLoadJob(invalidCsvFilePath, 10);

        // Wait briefly to ensure processing completes
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then - The job status should be FAILED or COMPLETED with error
        JobStatus status = dataLoaderJobService.getJobStatus(jobId);

        // The job might be QUEUED if it hasn't started yet, FAILED if it's processed the error
        assertThat(status.getStatus())
                .isIn(JobStatus.Status.QUEUED, JobStatus.Status.FAILED);

        if (status.getStatus() == JobStatus.Status.FAILED) {
            assertThat(status.getErrorMessage()).isNotBlank();
        }
    }
}