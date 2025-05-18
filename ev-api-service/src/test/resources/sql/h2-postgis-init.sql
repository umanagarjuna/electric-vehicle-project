
-- H2 PostGIS initialization script for testing
-- This script initializes spatial functions for H2 database

-- Enable H2GIS extension
CREATE ALIAS IF NOT EXISTS H2GIS_SPATIAL FOR "org.h2gis.functions.factory.H2GISFunctions.load";
CALL H2GIS_SPATIAL();

-- Create custom functions to emulate PostgreSQL PostGIS functions

-- ST_GeomFromText - Function to create geometry from WKT
CREATE ALIAS IF NOT EXISTS ST_GeomFromText FOR "org.h2gis.functions.spatial.convert.ST_GeomFromText.call";

-- ST_AsText - Function to convert geometry to WKT
CREATE ALIAS IF NOT EXISTS ST_AsText FOR "org.h2gis.functions.spatial.convert.ST_AsText.call";

-- ST_Distance - Function to get distance between geometries
CREATE ALIAS IF NOT EXISTS ST_Distance FOR "org.h2gis.functions.spatial.distance.ST_Distance.call";

-- ST_DWithin - Function to check if geometries are within a certain distance
CREATE ALIAS IF NOT EXISTS ST_DWithin FOR "org.h2gis.functions.spatial.distance.ST_DWithin.call";