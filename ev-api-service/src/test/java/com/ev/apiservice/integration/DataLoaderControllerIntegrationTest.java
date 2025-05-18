package com.ev.apiservice.integration;

import com.ev.apiservice.config.PostgresTestContainer;
import com.ev.apiservice.service.AsyncService;
import com.ev.apiservice.service.CsvDataLoaderService;
import com.ev.apiservice.service.DataLoaderJobService;
import com.ev.apiservice.service.DataLoaderJobService.JobStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(PostgresTestContainer.class)  // Use PostgreSQL container instead of H2GisTestConfig
@Transactional
public class DataLoaderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AsyncService asyncService;

    @MockBean
    private CsvDataLoaderService csvDataLoaderService;

    @Autowired
    private DataLoaderJobService jobService;

    @Test
    void loadDataAsynchronouslyShouldStartJobAndReturnImmediately() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",                   // parameter name
                "test-file.csv",          // original filename
                "text/csv",               // content type
                "sample,csv,content".getBytes() // content
        );

        int batchSize = 1000;

        // Create a CompletableFuture that completes immediately
        CompletableFuture<Integer> completedFuture = CompletableFuture.completedFuture(100);

        // When asyncService.executeLoadDataAsync is called, return the completed future
        when(asyncService.executeLoadDataAsync(anyString(), anyString(), anyInt()))
                .thenReturn(completedFuture);

        // When
        long startTime = System.currentTimeMillis();
        MvcResult result = mockMvc.perform(multipart("/api/v1/data-loader/load-csv")
                        .file(file)
                        .param("batchSize", String.valueOf(batchSize)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message", is("Data loading job started successfully")))
                .andExpect(jsonPath("$.jobId", notNullValue()))
                .andReturn();
        long endTime = System.currentTimeMillis();

        // Then
        // Request should return quickly
        assertThat(endTime - startTime).isLessThan(2000);

        // Verify service was called (note: now checking for anyString() for the file path since it's a temp file)
        verify(asyncService).executeLoadDataAsync(anyString(), anyString(), eq(batchSize));

        // Extract the job ID for status query test
        String responseJson = result.getResponse().getContentAsString();
        String jobId = objectMapper.readTree(responseJson).get("jobId").asText();

        // Update job status to COMPLETED for the next assertion
        JobStatus status = jobService.getJobStatus(jobId);
        status.setStatus(JobStatus.Status.COMPLETED);
        status.setRecordsProcessed(100);

        // Check the updated status
        mockMvc.perform(get("/api/v1/data-loader/job-status/{jobId}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId", is(jobId)))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.recordsProcessed", is(100)));
    }

    @Test
    void jobStatusShouldReflectProgressDuringExecution() throws Exception {
        // Given - Set up a completed future for the async service
        CompletableFuture<Integer> completedFuture = CompletableFuture.completedFuture(100);

        when(asyncService.executeLoadDataAsync(anyString(), anyString(), anyInt()))
                .thenReturn(completedFuture);

        // Create a mock file for upload
        MockMultipartFile file = new MockMultipartFile(
                "file",                   // parameter name
                "test-file.csv",          // original filename
                "text/csv",               // content type
                "sample,csv,content".getBytes() // content
        );

        // Start the job
        MvcResult result = mockMvc.perform(multipart("/api/v1/data-loader/load-csv")
                        .file(file)
                        .param("batchSize", "1000"))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        String jobId = objectMapper.readTree(responseJson).get("jobId").asText();

        // Update job status to RUNNING for the first assertion
        JobStatus status = jobService.getJobStatus(jobId);
        status.setStatus(JobStatus.Status.RUNNING);
        status.setTotalRecords(100);
        status.setRecordsProcessed(50);
        status.setProgress(50.0);

        // Verify status is RUNNING initially
        mockMvc.perform(get("/api/v1/data-loader/job-status/{jobId}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RUNNING")))
                .andExpect(jsonPath("$.progress", is(50.0)));

        // Update job status to simulate completion
        status.setStatus(JobStatus.Status.COMPLETED);
        status.setRecordsProcessed(100);
        status.setProgress(100.0);

        // Verify the final status is COMPLETED
        mockMvc.perform(get("/api/v1/data-loader/job-status/{jobId}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.recordsProcessed", is(100)));
    }

    @Test
    void getJobStatusShouldReturnNotFoundForNonExistentJob() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/data-loader/job-status/{jobId}", "non-existent-job"))
                .andExpect(status().isNotFound());
    }
}