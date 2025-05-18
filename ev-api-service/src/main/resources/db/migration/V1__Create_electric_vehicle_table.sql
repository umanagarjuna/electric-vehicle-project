-- Flyway migration script: V1__Create_electric_vehicle_table.sql
-- This script creates the main table for storing electric vehicle population data.

-- Ensure the PostGIS extension is enabled in your PostgreSQL database.
-- You might need to run this command manually via psql or your DB tool if not already enabled:
CREATE EXTENSION IF NOT EXISTS postgis;


CREATE TABLE electric_vehicle_population (
         vin VARCHAR(10) PRIMARY KEY, -- Vehicle Identification Number (Max 10 chars), Primary Key
         county VARCHAR(255),         -- County where the vehicle is registered
         city VARCHAR(255),           -- City where the vehicle is registered
         state VARCHAR(50),           -- State where the vehicle is registered (e.g., WA)
         postal_code VARCHAR(20),     -- Postal code
         model_year INT,              -- Model year of the vehicle
         make VARCHAR(255),           -- Manufacturer of the vehicle
         model VARCHAR(255),          -- Model of the vehicle
         electric_vehicle_type VARCHAR(100), -- Type of electric vehicle (e.g., BEV, PHEV)
         cafv_eligibility_status VARCHAR(255), -- Clean Alternative Fuel Vehicle (CAFV) eligibility
         electric_range INT,          -- Electric range of the vehicle in miles
         base_msrp NUMERIC(12, 2),    -- Base Manufacturer's Suggested Retail Price
         legislative_district VARCHAR(50), -- Legislative district
         dol_vehicle_id BIGINT UNIQUE NOT NULL, -- Department of Licensing Vehicle ID, must be unique and not null
         vehicle_location_point GEOMETRY(Point, 4326), -- Geographic location (latitude/longitude) using PostGIS Point
-- SRID 4326 corresponds to WGS84 coordinate system
         electric_utility VARCHAR(255), -- Electric utility provider
         census_tract_2020 BIGINT     -- 2020 Census Tract ID
);

-- Create indexes for frequently queried columns to improve performance
CREATE INDEX idx_ev_make_model ON electric_vehicle_population (make, model);
CREATE INDEX idx_ev_city_state ON electric_vehicle_population (city, state);
CREATE INDEX idx_ev_model_year ON electric_vehicle_population (model_year);
CREATE INDEX idx_ev_postal_code ON electric_vehicle_population (postal_code);
CREATE INDEX idx_ev_dol_vehicle_id ON electric_vehicle_population (dol_vehicle_id); -- Index on the unique DOL ID

-- Create a spatial index on the vehicle_location_point column
-- This is crucial for efficient execution of location-based (geospatial) queries
CREATE INDEX idx_ev_location_gist ON electric_vehicle_population USING GIST (vehicle_location_point);