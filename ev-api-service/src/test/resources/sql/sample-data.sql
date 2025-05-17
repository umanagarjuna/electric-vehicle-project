-- Sample data for integration and repository tests

-- First insert a TESLA Model 3
INSERT INTO electric_vehicle_population (
    vin, county, city, state, postal_code, model_year, make, model,
    electric_vehicle_type, cafv_eligibility_status, electric_range, base_msrp,
    legislative_district, dol_vehicle_id, vehicle_location_point, electric_utility, census_tract_2020
) VALUES (
             'SAMPLE1234', 'King', 'Seattle', 'WA', '98101', 2023, 'TESLA', 'Model 3',
             'Battery Electric Vehicle (BEV)', 'Clean Alternative Fuel Vehicle Eligible', 310, 45000.00,
             '43', 123456789, ST_SetSRID(ST_MakePoint(-122.33207, 47.60611), 4326), 'SEATTLE CITY LIGHT', 53033005600
         );

-- Add another TESLA Model 3 with different stats
INSERT INTO electric_vehicle_population (
    vin, county, city, state, postal_code, model_year, make, model,
    electric_vehicle_type, cafv_eligibility_status, electric_range, base_msrp,
    legislative_district, dol_vehicle_id, vehicle_location_point, electric_utility, census_tract_2020
) VALUES (
             'SAMPLE6789', 'King', 'Bellevue', 'WA', '98004', 2023, 'TESLA', 'Model 3',
             'Battery Electric Vehicle (BEV)', 'Clean Alternative Fuel Vehicle Eligible', 315, 46000.00,
             '48', 987654321, ST_SetSRID(ST_MakePoint(-122.20068, 47.61015), 4326), 'PUGET SOUND ENERGY', 53033022500
         );

-- Add a TESLA Model Y
INSERT INTO electric_vehicle_population (
    vin, county, city, state, postal_code, model_year, make, model,
    electric_vehicle_type, cafv_eligibility_status, electric_range, base_msrp,
    legislative_district, dol_vehicle_id, vehicle_location_point, electric_utility, census_tract_2020
) VALUES (
             'SAMPLEY123', 'Pierce', 'Tacoma', 'WA', '98402', 2023, 'TESLA', 'Model Y',
             'Battery Electric Vehicle (BEV)', 'Clean Alternative Fuel Vehicle Eligible', 330, 55000.00,
             '27', 456789123, ST_SetSRID(ST_MakePoint(-122.44062, 47.25288), 4326), 'TACOMA POWER', 53053071900
         );

-- Add a Ford Mustang Mach-E
INSERT INTO electric_vehicle_population (
    vin, county, city, state, postal_code, model_year, make, model,
    electric_vehicle_type, cafv_eligibility_status, electric_range, base_msrp,
    legislative_district, dol_vehicle_id, vehicle_location_point, electric_utility, census_tract_2020
) VALUES (
             'SAMPLEF123', 'Snohomish', 'Everett', 'WA', '98201', 2023, 'FORD', 'Mustang Mach-E',
             'Battery Electric Vehicle (BEV)', 'Clean Alternative Fuel Vehicle Eligible', 290, 49000.00,
             '38', 654321987, ST_SetSRID(ST_MakePoint(-122.20254, 47.97898), 4326), 'SNOHOMISH COUNTY PUD', 53061050300
         );

-- Add a Chevrolet Bolt
INSERT INTO electric_vehicle_population (
    vin, county, city, state, postal_code, model_year, make, model,
    electric_vehicle_type, cafv_eligibility_status, electric_range, base_msrp,
    legislative_district, dol_vehicle_id, vehicle_location_point, electric_utility, census_tract_2020
) VALUES (
             'SAMPLEC123', 'Clark', 'Vancouver', 'WA', '98660', 2022, 'CHEVROLET', 'Bolt EV',
             'Battery Electric Vehicle (BEV)', 'Clean Alternative Fuel Vehicle Eligible', 259, 38000.00,
             '49', 789123456, ST_SetSRID(ST_MakePoint(-122.67557, 45.63873), 4326), 'CLARK PUBLIC UTILITIES', 53011040900
         );

-- Add an older model from 2018
INSERT INTO electric_vehicle_population (
    vin, county, city, state, postal_code, model_year, make, model,
    electric_vehicle_type, cafv_eligibility_status, electric_range, base_msrp,
    legislative_district, dol_vehicle_id, vehicle_location_point, electric_utility, census_tract_2020
) VALUES (
             'SAMPLEN123', 'Kitsap', 'Bremerton', 'WA', '98337', 2018, 'NISSAN', 'LEAF',
             'Battery Electric Vehicle (BEV)', 'Clean Alternative Fuel Vehicle Eligible', 150, 32000.00,
             '26', 321987654, ST_SetSRID(ST_MakePoint(-122.62541, 47.56732), 4326), 'PUGET SOUND ENERGY', 53035090600
         );

-- Add a Plug-in Hybrid Electric Vehicle (PHEV)
INSERT INTO electric_vehicle_population (
    vin, county, city, state, postal_code, model_year, make, model,
    electric_vehicle_type, cafv_eligibility_status, electric_range, base_msrp,
    legislative_district, dol_vehicle_id, vehicle_location_point, electric_utility, census_tract_2020
) VALUES (
             'SAMPLEP123', 'Thurston', 'Olympia', 'WA', '98501', 2022, 'TOYOTA', 'Prius Prime',
             'Plug-in Hybrid Electric Vehicle (PHEV)', 'Clean Alternative Fuel Vehicle Eligible', 25, 31000.00,
             '22', 159357852, ST_SetSRID(ST_MakePoint(-122.90117, 47.03787), 4326), 'PUGET SOUND ENERGY', 53067011100
         );