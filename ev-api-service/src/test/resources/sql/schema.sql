
-- Schema for electric vehicle population table - Test Version (H2 Compatible)
DROP TABLE IF EXISTS electric_vehicle_population;

CREATE TABLE IF NOT EXISTS electric_vehicle_population (
                                                           id SERIAL PRIMARY KEY,
                                                           vin VARCHAR(10) UNIQUE NOT NULL,
                                                           county VARCHAR(50),
                                                           city VARCHAR(50),
                                                           state VARCHAR(2),
                                                           postal_code VARCHAR(10),
                                                           model_year INTEGER,
                                                           make VARCHAR(50),
                                                           model VARCHAR(100),
                                                           electric_vehicle_type VARCHAR(50),
                                                           cafv_eligibility_status VARCHAR(50),
                                                           electric_range INTEGER,
                                                           base_msrp NUMERIC(10,2),
                                                           legislative_district VARCHAR(20),
                                                           dol_vehicle_id BIGINT,
                                                           vehicle_location_point VARCHAR(100), -- Simplified for H2 compatibility
                                                           electric_utility VARCHAR(100),
                                                           census_tract_2020 BIGINT,
                                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Simple index without spatial specifics
CREATE INDEX IF NOT EXISTS idx_vehicle_vin ON electric_vehicle_population(vin);