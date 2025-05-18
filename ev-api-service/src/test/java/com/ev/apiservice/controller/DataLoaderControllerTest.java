package com.ev.apiservice.controller;

import com.ev.apiservice.service.DataLoaderJobService;
import com.ev.apiservice.service.DataLoaderJobService.JobStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DataLoaderController.class)
class DataLoaderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DataLoaderJobService jobService;

    @Test
    void loadDataAsynchronouslyShouldReturnAcceptedWithJobInfo() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",                  // parameter name
                "test-file.csv",         // original filename
                "text/csv",              // content type
                "sample,csv,content".getBytes() // content
        );

        String jobId = "test-job-id";
        when(jobService.startLoadJob(anyString(), anyInt())).thenReturn(jobId);

        // When and Then
        mockMvc.perform(multipart("/api/v1/data-loader/load-csv")
                        .file(file)
                        .param("batchSize", "1000"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message", is("Data loading job started successfully")))
                .andExpect(jsonPath("$.jobId", is(jobId)))
                .andExpect(jsonPath("$.statusEndpoint", is("/api/v1/data-loader/job-status/" + jobId)));
    }

    @Test
    void getJobStatusShouldReturnNotFoundForNonExistentJob() throws Exception {
        // Given
        String jobId = "non-existent-job";
        JobStatus notFoundStatus = new JobStatus(JobStatus.Status.NOT_FOUND, 0, 0);
        when(jobService.getJobStatus(eq(jobId))).thenReturn(notFoundStatus);

        // When and Then
        mockMvc.perform(get("/api/v1/data-loader/job-status/{jobId}", jobId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getJobStatusShouldReturnStatusForExistingJob() throws Exception {
        // Given
        String jobId = "existing-job";
        JobStatus runningStatus = new JobStatus(JobStatus.Status.RUNNING, 50, System.currentTimeMillis());
        runningStatus.setProgress(50.0);
        runningStatus.setTotalRecords(100);

        when(jobService.getJobStatus(eq(jobId))).thenReturn(runningStatus);

        // When and Then
        mockMvc.perform(get("/api/v1/data-loader/job-status/{jobId}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId", is(jobId)))
                .andExpect(jsonPath("$.status", is("RUNNING")))
                .andExpect(jsonPath("$.recordsProcessed", is(50)))
                .andExpect(jsonPath("$.progress", is(50.0)))
                .andExpect(jsonPath("$.startTime", notNullValue()));
    }

    @Test
    void getJobStatusShouldIncludeErrorMessageForFailedJob() throws Exception {
        // Given
        String jobId = "failed-job";
        JobStatus failedStatus = new JobStatus(JobStatus.Status.FAILED, 25, System.currentTimeMillis());
        failedStatus.setErrorMessage("Error occurred during processing");

        when(jobService.getJobStatus(eq(jobId))).thenReturn(failedStatus);

        // When and Then
        mockMvc.perform(get("/api/v1/data-loader/job-status/{jobId}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId", is(jobId)))
                .andExpect(jsonPath("$.status", is("FAILED")))
                .andExpect(jsonPath("$.recordsProcessed", is(25)))
                .andExpect(jsonPath("$.errorMessage", is("Error occurred during processing")))
                .andExpect(jsonPath("$.startTime", notNullValue()));
    }
}