package com.ev.apiservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataLoaderJobServiceTest {

    @Mock
    private CsvDataLoaderService csvDataLoaderService;

    @Mock
    private AsyncService asyncService;

    private DataLoaderJobService dataLoaderJobService;

    @BeforeEach
    void setUp() {
        dataLoaderJobService = new DataLoaderJobService(csvDataLoaderService, asyncService);
    }

    @Test
    void startLoadJobShouldQueueJobAndReturnJobId() {
        // Given
        String csvFilePath = "file:path/to/csv";
        int batchSize = 1000;

        when(asyncService.executeLoadDataAsync(anyString(), eq(csvFilePath), eq(batchSize)))
                .thenReturn(CompletableFuture.completedFuture(100));

        // When
        String jobId = dataLoaderJobService.startLoadJob(csvFilePath, batchSize);

        // Then
        assertNotNull(jobId);
        assertTrue(jobId.length() > 0);

        verify(asyncService).executeLoadDataAsync(eq(jobId), eq(csvFilePath), eq(batchSize));

        // Verify job was queued with correct status
        DataLoaderJobService.JobStatus status = dataLoaderJobService.getJobStatus(jobId);
        assertEquals(DataLoaderJobService.JobStatus.Status.QUEUED, status.getStatus());
        assertEquals(0, status.getRecordsProcessed());
    }

    @Test
    void getJobStatusShouldReturnNotFoundForNonExistentJob() {
        // Given
        String nonExistentJobId = "non-existent-id";

        // When
        DataLoaderJobService.JobStatus status = dataLoaderJobService.getJobStatus(nonExistentJobId);

        // Then
        assertEquals(DataLoaderJobService.JobStatus.Status.NOT_FOUND, status.getStatus());
    }

    @Test
    void getJobStatusShouldReturnCorrectStatusForExistingJob() {
        // Given
        String csvFilePath = "file:path/to/csv";
        int batchSize = 1000;

        when(asyncService.executeLoadDataAsync(anyString(), anyString(), anyInt()))
                .thenReturn(CompletableFuture.completedFuture(100));

        String jobId = dataLoaderJobService.startLoadJob(csvFilePath, batchSize);

        // When
        DataLoaderJobService.JobStatus status = dataLoaderJobService.getJobStatus(jobId);

        // Then
        assertNotNull(status);
        assertEquals(DataLoaderJobService.JobStatus.Status.QUEUED, status.getStatus());
    }
}