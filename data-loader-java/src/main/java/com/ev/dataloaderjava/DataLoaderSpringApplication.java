package com.ev.dataloaderjava;

import com.ev.dataloaderjava.service.CsvDataLoaderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class DataLoaderSpringApplication implements CommandLineRunner {

    private final CsvDataLoaderService csvDataLoaderService;

    public DataLoaderSpringApplication(CsvDataLoaderService csvDataLoaderService) {
        this.csvDataLoaderService = csvDataLoaderService;
    }

    public static void main(String[] args) {
        log.info("STARTING THE EV DATA LOADER APPLICATION");
        SpringApplication.run(DataLoaderSpringApplication.class, args);
        log.info("EV DATA LOADER APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("EXECUTING : command line runner");
        try {
            csvDataLoaderService.loadData();
            log.info("Data loading process completed successfully through CommandLineRunner.");
        } catch (Exception e) {
            log.error("Error during data loading process through CommandLineRunner: ", e);
        }
    }
}
