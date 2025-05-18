package com.ev.apiclientjava.command;

import com.ev.apiclientjava.dto.ElectricVehicleInputDTO;
import com.ev.apiclientjava.dto.PointInputDTO;
import com.ev.apiclientjava.util.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

/**
 * Picocli command to create a new electric vehicle record via the API.
 * Data can be provided via a JSON file or individual command-line options.
 */
@Command(name = "create",
        description = "Create a new electric vehicle record.",
        mixinStandardHelpOptions = true)
public class CreateVehicleCommand implements Callable<Integer> {

    @Option(names = {"--api-base-url"}, description = "Base URL of the EV API.",
            defaultValue = "http://localhost:8080/api/v1", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private String apiBaseUrl;

    @Option(names = {"--file", "-f"}, description = "Path to a JSON file containing vehicle data. " +
            "If provided, individual field options are ignored.")
    private String filePath;

    // Individual field options, used if --file is not specified
    @Option(names = "--vin", description = "Vehicle Identification Number (Max 10 chars). Required if --file not used.") String vin;
    @Option(names = "--make", description = "Make of the vehicle. Required if --file not used.") String make;
    @Option(names = "--model", description = "Model of the vehicle. Required if --file not used.") String model;
    @Option(names = "--year", description = "Model year. Required if --file not used.") Integer modelYear;
    @Option(names = "--dol-id", description = "DOL Vehicle ID. Required if --file not used.") Long dolVehicleId;
    @Option(names = "--county", description = "County") String county;
    @Option(names = "--city", description = "City") String city;
    @Option(names = "--state", description = "State abbreviation (e.g., WA)") String stateAbbreviation;
    @Option(names = "--zip", description = "Postal Code") String postalCode;
    @Option(names = "--ev-type", description = "Electric Vehicle Type (e.g., BEV, PHEV)") String evType;
    @Option(names = "--cafv-status", description = "CAFV Eligibility Status") String cafvStatus;
    @Option(names = "--range", description = "Electric Range in miles") Integer electricRange;
    @Option(names = "--msrp", description = "Base MSRP (e.g., 39990.00)") BigDecimal baseMsrp;
    @Option(names = "--district", description = "Legislative District") String legislativeDistrict;
    @Option(names = "--longitude", description = "Longitude for vehicle location (e.g., -122.3321)") Double longitude;
    @Option(names = "--latitude", description = "Latitude for vehicle location (e.g., 47.6062)") Double latitude;
    @Option(names = "--utility", description = "Electric Utility provider") String electricUtility;
    @Option(names = "--census-tract", description = "2020 Census Tract ID") Long censusTract;

    private final ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();

    @Override
    public Integer call() throws Exception {
        ElectricVehicleInputDTO vehicleData;

        if (filePath != null) {
            System.out.println("Reading vehicle data from file: " + filePath);
            try {
                String jsonInput = Files.readString(Paths.get(filePath));
                vehicleData = objectMapper.readValue(jsonInput, ElectricVehicleInputDTO.class);
            } catch (Exception e) {
                System.err.println("Error reading or parsing JSON file '" + filePath + "': " + e.getMessage());
                return CommandLine.ExitCode.USAGE;
            }
        } else {
            // Validate required fields if not using file input
            if (vin == null || make == null || model == null || modelYear == null || dolVehicleId == null) {
                System.err.println("Error: --vin, --make, --model, --year, and --dol-id are required if --file is not used.");
                return CommandLine.ExitCode.USAGE;
            }
            System.out.println("Constructing vehicle data from command line options...");
            vehicleData = new ElectricVehicleInputDTO();
            vehicleData.setVin(vin);
            vehicleData.setMake(make);
            vehicleData.setModel(model);
            vehicleData.setModelYear(modelYear);
            vehicleData.setDolVehicleId(dolVehicleId);
            vehicleData.setCounty(county); // Optional fields
            vehicleData.setCity(city);
            vehicleData.setState(stateAbbreviation);
            vehicleData.setPostalCode(postalCode);
            vehicleData.setElectricVehicleType(evType);
            vehicleData.setCafvEligibilityStatus(cafvStatus);
            vehicleData.setElectricRange(electricRange);
            vehicleData.setBaseMSRP(baseMsrp);
            vehicleData.setLegislativeDistrict(legislativeDistrict);
            if (longitude != null && latitude != null) {
                vehicleData.setVehicleLocation(new PointInputDTO(longitude, latitude));
            }
            vehicleData.setElectricUtility(electricUtility);
            vehicleData.setCensusTract2020(censusTract);
        }

        String requestBody = objectMapper.writeValueAsString(vehicleData);
        // Log the request body being sent for debugging purposes
        System.out.println("Request Body for Create:\n" + requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiBaseUrl + "/vehicles"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        try {
            HttpResponse<String> response = HttpClientUtil.sendRequest(request);
            // HTTP 201 Created indicates success
            return (response.statusCode() == 201) ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
        } catch (Exception e) {
            System.err.println("An error occurred while creating vehicle: " + e.getMessage());
            return CommandLine.ExitCode.SOFTWARE;
        }
    }
}