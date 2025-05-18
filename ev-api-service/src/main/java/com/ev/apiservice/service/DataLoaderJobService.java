package com.ev.apiservice.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing asynchronous data loading jobs.
 */
@Service
@Slf4j
public class DataLoaderJobService {

    private final Map<String, JobStatus> jobStatusMap = new ConcurrentHashMap<>();
    private final CsvDataLoaderService csvDataLoaderService;
    private final AsyncService asyncService;

    @Autowired
    public DataLoaderJobService(CsvDataLoaderService csvDataLoaderService, @Lazy AsyncService asyncService) {
        this.csvDataLoaderService = csvDataLoaderService;
        this.asyncService = asyncService;
    }

    /**
     * Starts an asynchronous data loading job.
     *
     * @param csvFilePath Path to the CSV file
     * @param batchSize Number of records to process in each batch
     * @return The job ID
     */
    public String startLoadJob(String csvFilePath, int batchSize) {
        String jobId = UUID.randomUUID().toString();
        jobStatusMap.put(jobId, new JobStatus(JobStatus.Status.QUEUED, 0, System.currentTimeMillis()));

        // Start the async process using the AsyncService
        asyncService.executeLoadDataAsync(jobId, csvFilePath, batchSize);

        log.info("Queued asynchronous data loading job {} for file: {}", jobId, csvFilePath);
        return jobId;
    }

    /**
     * Gets the status of a data loading job.
     *
     * @param jobId The job ID
     * @return The job status
     */
    public JobStatus getJobStatus(String jobId) {
        return jobStatusMap.getOrDefault(jobId,
                new JobStatus(JobStatus.Status.NOT_FOUND, 0, 0));
    }

    /**
     * Data class for tracking job status.
     */
    @Data
    public static class JobStatus {
        public enum Status { QUEUED, RUNNING, COMPLETED, FAILED, NOT_FOUND }

        private Status status;
        private int recordsProcessed;
        private int totalRecords;
        private long startTime;
        private String errorMessage;
        private double progress; // 0-100 percentage

        public JobStatus(Status status, int recordsProcessed, long startTime) {
            this.status = status;
            this.recordsProcessed = recordsProcessed;
            this.startTime = startTime > 0 ? startTime : System.currentTimeMillis();
            this.progress = 0.0;
        }
    }
}