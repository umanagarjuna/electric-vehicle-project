package com.ev.apiservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Electric Vehicle API Service.
 * This class initializes and runs the Spring Boot application.
 */
@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Electric Vehicle Population API",
                version = "v1",
                description = "This API provides CRUD operations and batch updates for electric vehicle population data.",
                license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.html")
        )
)
public class EvApiApplication {

    /**
     * Main method to start the Spring Boot application.
     * @param args Command line arguments passed to the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(EvApiApplication.class, args);
    }
}