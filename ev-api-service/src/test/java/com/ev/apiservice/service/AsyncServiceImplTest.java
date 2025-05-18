package com.ev.apiservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncServiceImplTest {

    @Mock
    private CsvDataLoaderService csvDataLoaderService;

    @Mock
    private DataLoaderJobService dataLoaderJobService;

    @Mock
    private DataLoaderJobService.JobStatus jobStatus;

    private AsyncServiceImpl asyncService;

    @BeforeEach
    void setUp() {
        asyncService = new AsyncServiceImpl(csvDataLoaderService, dataLoaderJobService);

        // Setup default mocks
        when(dataLoaderJobService.getJobStatus(anyString())).thenReturn(jobStatus);
    }

    @Test
    void executeLoadDataAsyncShouldCompleteSuccessfullyWhenDataLoadingSucceeds() throws Exception {
        // Given
        String jobId = "test-job-id";
        String csvFilePath = "file:path/to/file.csv";
        int batchSize = 1000;
        int recordCount = 100;

        when(csvDataLoaderService.countRecords(eq(csvFilePath))).thenReturn(recordCount);
        when(csvDataLoaderService.loadData(eq(csvFilePath), eq(batchSize), any())).thenReturn(recordCount);

        // When
        CompletableFuture<Integer> future = asyncService.executeLoadDataAsync(jobId, csvFilePath, batchSize);

        // Then
        assertTrue(future.isDone());
        assertEquals(recordCount, future.get());

        // Verify status updates
        verify(jobStatus).setStatus(DataLoaderJobService.JobStatus.Status.RUNNING);
        verify(jobStatus).setTotalRecords(recordCount);
        verify(jobStatus).setRecordsProcessed(recordCount);
        verify(jobStatus).setStatus(DataLoaderJobService.JobStatus.Status.COMPLETED);
        verify(jobStatus).setProgress(100.0);
    }

    @Test
    void executeLoadDataAsyncShouldHandleExceptionWhenCountRecordsFails() throws Exception {
        // Given
        String jobId = "test-job-id";
        String csvFilePath = "file:path/to/file.csv";
        int batchSize = 1000;
        int recordCount = 100;

        RuntimeException countException = new RuntimeException("Error counting records");
        when(csvDataLoaderService.countRecords(eq(csvFilePath))).thenThrow(countException);
        when(csvDataLoaderService.loadData(eq(csvFilePath), eq(batchSize), any())).thenReturn(recordCount);

        // When
        CompletableFuture<Integer> future = asyncService.executeLoadDataAsync(jobId, csvFilePath, batchSize);

        // Then
        assertTrue(future.isDone());
        assertEquals(recordCount, future.get());

        // Verify status updates
        verify(jobStatus).setStatus(DataLoaderJobService.JobStatus.Status.RUNNING);
        verify(jobStatus, never()).setTotalRecords(anyInt()); // This should not be called
        verify(jobStatus).setRecordsProcessed(recordCount);
        verify(jobStatus).setStatus(DataLoaderJobService.JobStatus.Status.COMPLETED);
        verify(jobStatus).setProgress(100.0);
    }

    @Test
    void executeLoadDataAsyncShouldHandleExceptionWhenLoadDataFails() {
        // Given
        String jobId = "test-job-id";
        String csvFilePath = "file:path/to/file.csv";
        int batchSize = 1000;

        RuntimeException loadException = new RuntimeException("Error loading data");
        when(csvDataLoaderService.countRecords(eq(csvFilePath))).thenReturn(100);
        when(csvDataLoaderService.loadData(eq(csvFilePath), eq(batchSize), any())).thenThrow(loadException);

        // When
        CompletableFuture<Integer> future = asyncService.executeLoadDataAsync(jobId, csvFilePath, batchSize);

        // Then
        assertTrue(future.isCompletedExceptionally());

        // Verify status updates
        verify(jobStatus).setStatus(DataLoaderJobService.JobStatus.Status.RUNNING);
        verify(jobStatus).setTotalRecords(100);
        verify(jobStatus).setStatus(DataLoaderJobService.JobStatus.Status.FAILED);
        verify(jobStatus).setErrorMessage(loadException.getMessage());
    }
}