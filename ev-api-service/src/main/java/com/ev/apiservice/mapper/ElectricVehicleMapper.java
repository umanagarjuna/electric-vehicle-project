package com.ev.apiservice.mapper;

import com.ev.apiservice.dto.CreateElectricVehicleDTO;
import com.ev.apiservice.dto.ElectricVehicleDTO;
import com.ev.apiservice.dto.PointDTO;
import com.ev.apiservice.model.ElectricVehicle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

/**
 * Component responsible for mapping between ElectricVehicle JPA entities and their DTOs.
 */
@Component
public class ElectricVehicleMapper {

    // GeometryFactory for creating JTS Point objects. SRID 4326 for WGS84.
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * Converts an ElectricVehicle entity to an ElectricVehicleDTO.
     * @param entity The ElectricVehicle entity.
     * @return The corresponding ElectricVehicleDTO, or null if the entity is null.
     */
    public ElectricVehicleDTO toDTO(ElectricVehicle entity) {
        if (entity == null) {
            return null;
        }
        ElectricVehicleDTO dto = new ElectricVehicleDTO();
        dto.setVin(entity.getVin());
        dto.setCounty(entity.getCounty());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setPostalCode(entity.getPostalCode());
        dto.setModelYear(entity.getModelYear());
        dto.setMake(entity.getMake());
        dto.setModel(entity.getModel());
        dto.setElectricVehicleType(entity.getElectricVehicleType());
        dto.setCafvEligibilityStatus(entity.getCafvEligibilityStatus());
        dto.setElectricRange(entity.getElectricRange());
        dto.setBaseMSRP(entity.getBaseMSRP());
        dto.setLegislativeDistrict(entity.getLegislativeDistrict());
        dto.setDolVehicleId(entity.getDolVehicleId());
        if (entity.getVehicleLocationPoint() != null) {
            // JTS Point: X coordinate is longitude, Y coordinate is latitude
            dto.setVehicleLocation(new PointDTO(entity.getVehicleLocationPoint().getX(), entity.getVehicleLocationPoint().getY()));
        }
        dto.setElectricUtility(entity.getElectricUtility());
        dto.setCensusTract2020(entity.getCensusTract2020());
        return dto;
    }

    /**
     * Converts a CreateElectricVehicleDTO to an ElectricVehicle entity.
     * @param dto The CreateElectricVehicleDTO.
     * @return The corresponding ElectricVehicle entity, or null if the DTO is null.
     */
    public ElectricVehicle toEntity(CreateElectricVehicleDTO dto) {
        if (dto == null) {
            return null;
        }
        ElectricVehicle entity = new ElectricVehicle();
        entity.setVin(dto.getVin());
        entity.setCounty(dto.getCounty());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setPostalCode(dto.getPostalCode());
        entity.setModelYear(dto.getModelYear());
        entity.setMake(dto.getMake());
        entity.setModel(dto.getModel());
        entity.setElectricVehicleType(dto.getElectricVehicleType());
        entity.setCafvEligibilityStatus(dto.getCafvEligibilityStatus());
        entity.setElectricRange(dto.getElectricRange());
        entity.setBaseMSRP(dto.getBaseMSRP());
        entity.setLegislativeDistrict(dto.getLegislativeDistrict());
        entity.setDolVehicleId(dto.getDolVehicleId());
        if (dto.getVehicleLocation() != null &&
                dto.getVehicleLocation().getLongitude() != null &&
                dto.getVehicleLocation().getLatitude() != null) {
            // Create a JTS Point from longitude and latitude
            entity.setVehicleLocationPoint(
                    geometryFactory.createPoint(new Coordinate(dto.getVehicleLocation().getLongitude(), dto.getVehicleLocation().getLatitude()))
            );
        }
        entity.setElectricUtility(dto.getElectricUtility());
        entity.setCensusTract2020(dto.getCensusTract2020());
        return entity;
    }

    /**
     * Updates an existing ElectricVehicle entity with data from an ElectricVehicleDTO.
     * Note: VIN (primary key) and potentially dolVehicleId (unique business key) are typically not updated.
     * @param dto The ElectricVehicleDTO containing updated data.
     * @param entity The ElectricVehicle entity to update.
     */
    public void updateEntityFromDto(ElectricVehicleDTO dto, ElectricVehicle entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setCounty(dto.getCounty());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setPostalCode(dto.getPostalCode());
        entity.setModelYear(dto.getModelYear());
        entity.setMake(dto.getMake());
        entity.setModel(dto.getModel());
        entity.setElectricVehicleType(dto.getElectricVehicleType());
        entity.setCafvEligibilityStatus(dto.getCafvEligibilityStatus());
        entity.setElectricRange(dto.getElectricRange());
        entity.setBaseMSRP(dto.getBaseMSRP());
        entity.setLegislativeDistrict(dto.getLegislativeDistrict());

        if (dto.getVehicleLocation() != null &&
                dto.getVehicleLocation().getLongitude() != null &&
                dto.getVehicleLocation().getLatitude() != null) {
            entity.setVehicleLocationPoint(
                    geometryFactory.createPoint(new Coordinate(dto.getVehicleLocation().getLongitude(), dto.getVehicleLocation().getLatitude()))
            );
        } else {
            // If DTO location is null, clear the entity's location.
            // If DTO location is present but lat/lon are null, this will also result in a null point.
            entity.setVehicleLocationPoint(null);
        }
        entity.setElectricUtility(dto.getElectricUtility());
        entity.setCensusTract2020(dto.getCensusTract2020());
    }
}