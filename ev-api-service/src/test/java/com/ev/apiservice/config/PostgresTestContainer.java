package com.ev.apiservice.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;

@TestConfiguration
public class PostgresTestContainer {

    @Bean(destroyMethod = "stop")
    public PostgreSQLContainer<?> postgreSQLContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(
                DockerImageName.parse("postgis/postgis:14-3.2")
                        .asCompatibleSubstituteFor("postgres"))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");

        container.start();

        // Set system properties for Spring to use
        System.setProperty("spring.datasource.url", container.getJdbcUrl());
        System.setProperty("spring.datasource.username", container.getUsername());
        System.setProperty("spring.datasource.password", container.getPassword());
        System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");

        // Explicitly set the Hibernate dialect to PostgreSQL
        System.setProperty("spring.jpa.properties.hibernate.dialect", "org.hibernate.spatial.dialect.postgis.PostgisPG95Dialect");
        System.setProperty("spring.jpa.database-platform", "org.hibernate.spatial.dialect.postgis.PostgisPG95Dialect");

        return container;
    }

    @Bean
    @DependsOn("postgreSQLContainer")
    public DataSource dataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(System.getProperty("spring.datasource.url"));
        hikariConfig.setUsername(System.getProperty("spring.datasource.username"));
        hikariConfig.setPassword(System.getProperty("spring.datasource.password"));
        hikariConfig.setDriverClassName("org.postgresql.Driver");

        return new HikariDataSource(hikariConfig);
    }

    @Bean
    @DependsOn("dataSource")
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @DependsOn("jdbcTemplate")
    public Boolean createSchema(JdbcTemplate jdbcTemplate) {
        // Enable PostGIS extension
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS postgis");

        // Create the electric vehicle table with spatial support
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS electric_vehicle_population (" +
                "vin VARCHAR(10) PRIMARY KEY," +
                "county VARCHAR(255)," +
                "city VARCHAR(255)," +
                "state VARCHAR(50)," +
                "postal_code VARCHAR(20)," +
                "model_year INTEGER," +
                "make VARCHAR(255)," +
                "model VARCHAR(255)," +
                "electric_vehicle_type VARCHAR(100)," +
                "cafv_eligibility_status VARCHAR(255)," +
                "electric_range INTEGER," +
                "base_msrp NUMERIC(12,2)," +
                "legislative_district VARCHAR(50)," +
                "dol_vehicle_id BIGINT NOT NULL UNIQUE," +
                "vehicle_location_point GEOMETRY(Point,4326)," +
                "electric_utility VARCHAR(255)," +
                "census_tract_2020 BIGINT" +
                ")");

        return true;
    }
}