package com.ev.apiclientjava.dto;

/**
 * Data Transfer Object for sending geographic point coordinates (longitude and latitude)
 * from the client to the API.
 */
public class PointInputDTO {
    private Double longitude;
    private Double latitude;

    /**
     * Default constructor, required for Jackson deserialization if used.
     */
    public PointInputDTO() {}

    /**
     * Constructs a PointInputDTO with specified longitude and latitude.
     * @param longitude The longitude (X coordinate).
     * @param latitude The latitude (Y coordinate).
     */
    public PointInputDTO(Double longitude, Double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    // Standard Getters and Setters
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
}