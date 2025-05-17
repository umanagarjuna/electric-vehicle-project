package com.ev.apiservice.config;

import org.h2gis.functions.factory.H2GISFunctions;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;

import javax.sql.DataSource;
import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.SQLException;

@TestConfiguration
public class H2GisTestConfig {

    private final DataSource dataSource;

    public H2GisTestConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void initializeH2GIS() throws SQLException {
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            // Initialize H2GIS - this registers all spatial functions
            H2GISFunctions.load(connection);
        }
    }
}