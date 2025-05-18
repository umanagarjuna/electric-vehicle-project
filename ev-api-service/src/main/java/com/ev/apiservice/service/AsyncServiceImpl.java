package com.ev.apiservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Implementation of AsyncService for handling asynchronous data loading operations.
 */
@Service
@Slf4j
public class AsyncServiceImpl implements AsyncService {

    private final CsvDataLoaderService csvDataLoaderService;
    private final DataLoaderJobService dataLoaderJobService;

    @Autowired
    public AsyncServiceImpl(CsvDataLoaderService csvDataLoaderService, DataLoaderJobService dataLoaderJobService) {
        this.csvDataLoaderService = csvDataLoaderService;
        this.dataLoaderJobService = dataLoaderJobService;
    }

    /**
     * Executes data loading asynchronously.
     *
     * @param jobId The ID of the job
     * @param csvFilePath Path to the CSV file
     * @param batchSize Number of records to process in each batch
     * @return CompletableFuture containing the number of records processed
     */
    @Async("taskExecutor")
    @Override
    public CompletableFuture<Integer> executeLoadDataAsync(String jobId, String csvFilePath, int batchSize) {
        DataLoaderJobService.JobStatus status = dataLoaderJobService.getJobStatus(jobId);
        status.setStatus(DataLoaderJobService.JobStatus.Status.RUNNING);

        try {
            log.info("Starting asynchronous data loading for job {} in thread {}: {}",
                    jobId, Thread.currentThread().getName(), csvFilePath);

            int recordsProcessed;

            // First pass to estimate total records for progress tracking
            final int totalRecords;
            int totalRecords1;
            try {
                int count = csvDataLoaderService.countRecords(csvFilePath);
                status.setTotalRecords(count);
                totalRecords1 = count;
            } catch (Exception e) {
                log.warn("Unable to determine total record count for progress tracking: {}", e.getMessage());
                totalRecords1 = 0;
            }

            // Process in smaller chunks and update progress
            totalRecords = totalRecords1;
            recordsProcessed = csvDataLoaderService.loadData(csvFilePath, batchSize, (processed) -> {
                status.setRecordsProcessed(processed);
                if (totalRecords > 0) {
                    status.setProgress((double) processed / totalRecords * 100);
                }
            });

            status.setRecordsProcessed(recordsProcessed);
            status.setStatus(DataLoaderJobService.JobStatus.Status.COMPLETED);
            status.setProgress(100.0);

            log.info("Completed asynchronous data loading for job {} in thread {}. Records processed: {}",
                    jobId, Thread.currentThread().getName(), recordsProcessed);
            return CompletableFuture.completedFuture(recordsProcessed);
        } catch (Exception e) {
            log.error("Error during asynchronous data loading for job {}: {}", jobId, e.getMessage(), e);
            status.setStatus(DataLoaderJobService.JobStatus.Status.FAILED);
            status.setErrorMessage(e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
}