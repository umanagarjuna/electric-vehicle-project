package com.ev.apiservice.service;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for asynchronous data loading operations.
 */
public interface AsyncService {

    /**
     * Executes data loading asynchronously.
     *
     * @param jobId The ID of the job
     * @param csvFilePath Path to the CSV file
     * @param batchSize Number of records to process in each batch
     * @return CompletableFuture containing the number of records processed
     */
    CompletableFuture<Integer> executeLoadDataAsync(String jobId, String csvFilePath, int batchSize);
}