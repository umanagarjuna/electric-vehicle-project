package com.ev.apiclientjava.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Data Transfer Object to help Jackson deserialize the Page object returned by Spring Data REST / Spring MVC.
 * It captures common pagination fields.
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)} is crucial because Spring's PageImpl
 * has other properties (like 'pageable', 'sort') that we might not need to map here.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginatedVehicleResponse {

    // The actual content of the page (list of vehicles)
    private List<ElectricVehicleInputDTO> content; // Using ElectricVehicleInputDTO for simplicity

    @JsonProperty("totalPages") // Total number of pages available
    private int totalPages;

    @JsonProperty("totalElements") // Total number of elements across all pages
    private long totalElements;

    @JsonProperty("numberOfElements") // Number of elements in the current page
    private int numberOfElements;

    @JsonProperty("size") // The requested page size
    private int size;

    @JsonProperty("number") // The current page number (0-indexed)
    private int number;

    @JsonProperty("first") // True if this is the first page
    private boolean first;

    @JsonProperty("last") // True if this is the last page
    private boolean last;

    @JsonProperty("empty") // True if the content list is empty
    private boolean empty;

    // Standard Getters and Setters for all fields
    public List<ElectricVehicleInputDTO> getContent() { return content; }
    public void setContent(List<ElectricVehicleInputDTO> content) { this.content = content; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public int getNumberOfElements() { return numberOfElements; }
    public void setNumberOfElements(int numberOfElements) { this.numberOfElements = numberOfElements; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public boolean isFirst() { return first; }
    public void setFirst(boolean first) { this.first = first; }
    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }
    public boolean isEmpty() { return empty; }
    public void setEmpty(boolean empty) { this.empty = empty; }
}
