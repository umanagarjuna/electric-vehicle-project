package com.ev.apiservice.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    private final MeterRegistry meterRegistry;

    public MetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    // API request counters
    @Bean
    public Counter vehicleCreationCounter() {
        return Counter.builder("api.vehicle.creation")
                .description("Count of vehicle creation requests")
                .register(meterRegistry);
    }

    @Bean
    public Counter vehicleUpdateCounter() {
        return Counter.builder("api.vehicle.update")
                .description("Count of vehicle update requests")
                .register(meterRegistry);
    }

    @Bean
    public Counter vehicleDeletionCounter() {
        return Counter.builder("api.vehicle.deletion")
                .description("Count of vehicle deletion requests")
                .register(meterRegistry);
    }

    @Bean
    public Counter batchUpdateCounter() {
        return Counter.builder("api.vehicle.batch_update")
                .description("Count of batch update operations")
                .register(meterRegistry);
    }

    // Timers for operation duration
    @Bean
    public Timer apiRequestTimer() {
        return Timer.builder("api.request.duration")
                .description("API request duration in seconds")
                .register(meterRegistry);
    }

    @Bean
    public Timer databaseOperationTimer() {
        return Timer.builder("database.operation.duration")
                .description("Database operation duration in seconds")
                .register(meterRegistry);
    }
}