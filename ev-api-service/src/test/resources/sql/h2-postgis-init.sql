-- H2 database initialization with PostGIS-like functions for testing

-- Create extension aliases for H2 to mimic PostGIS functionality
CREATE ALIAS IF NOT EXISTS ST_MakePoint FOR "org.h2gis.functions.spatial.create.ST_MakePoint.makePoint";
CREATE ALIAS IF NOT EXISTS ST_SetSRID FOR "org.h2gis.functions.spatial.srid.ST_SetSRID.setSRID";
CREATE ALIAS IF NOT EXISTS ST_AsText FOR "org.h2gis.functions.spatial.convert.ST_AsText.asText";
CREATE ALIAS IF NOT EXISTS ST_GeomFromText FOR "org.h2gis.functions.spatial.convert.ST_GeomFromText.geomFromText";
CREATE ALIAS IF NOT EXISTS ST_Distance FOR "org.h2gis.functions.spatial.distance.ST_Distance.distance";
CREATE ALIAS IF NOT EXISTS ST_X FOR "org.h2gis.functions.spatial.properties.ST_X.X";
CREATE ALIAS IF NOT EXISTS ST_Y FOR "org.h2gis.functions.spatial.properties.ST_Y.Y";

-- Create spatial tables needed for H2GIS functionality
CREATE DOMAIN IF NOT EXISTS GEOMETRY AS BLOB;