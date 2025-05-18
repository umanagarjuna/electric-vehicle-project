package com.ev.apiclientjava.command;

import com.ev.apiclientjava.dto.ElectricVehicleInputDTO;
import com.ev.apiclientjava.util.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

/**
 * Picocli command to update an existing electric vehicle record via the API.
 * Requires a JSON file for the updated vehicle data.
 */
@Command(name = "update",
        description = "Update an existing electric vehicle record using data from a JSON file.",
        mixinStandardHelpOptions = true)
public class UpdateVehicleCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The VIN of the vehicle to update.", arity = "1")
    private String vinToUpdate;

    @Option(names = {"--api-base-url"}, description = "Base URL of the EV API.", defaultValue = "http://localhost:8080/api/v1", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private String apiBaseUrl;

    @Option(names = {"--file", "-f"}, description = "Path to a JSON file containing the updated vehicle data. The VIN in the file should match the VIN parameter or will be aligned.", required = true)
    private String filePath;

    private final ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();

    @Override
    public Integer call() throws Exception {
        ElectricVehicleInputDTO vehicleData;

        System.out.println("Reading updated vehicle data from file: " + filePath);
        try {
            String jsonInput = Files.readString(Paths.get(filePath));
            vehicleData = objectMapper.readValue(jsonInput, ElectricVehicleInputDTO.class);
        } catch (Exception e) {
            System.err.println("Error reading or parsing JSON file '" + filePath + "': " + e.getMessage());
            return CommandLine.ExitCode.USAGE;
        }

        // The VIN in the path is authoritative for identifying the resource to update.
        // If the DTO's VIN is different, log a warning and align it for the request body.
        if (vehicleData.getVin() != null && !vehicleData.getVin().equals(vinToUpdate)) {
            System.out.println("Warning: VIN in JSON file ('" + vehicleData.getVin() +
                    "') differs from VIN in path ('" + vinToUpdate +
                    "'). The VIN in the path will be used to identify the resource. " +
                    "The VIN in the request body will be set to match the path VIN for this PUT request.");
        }
        // Ensure the DTO VIN is set to the path VIN for the request body,
        // as PUT typically replaces the resource identified by the URL.
        vehicleData.setVin(vinToUpdate);

        String requestBody = objectMapper.writeValueAsString(vehicleData);
        // Log the request body being sent for debugging purposes
        System.out.println("Request Body for Update:\n" + requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiBaseUrl + "/vehicles/" + vinToUpdate))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody)) // HTTP PUT for full update
                .build();
        try {
            HttpResponse<String> response = HttpClientUtil.sendRequest(request);
            // HTTP 200 OK indicates success for PUT if resource is updated
            return (response.statusCode() == 200) ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
        } catch (Exception e) {
            System.err.println("An error occurred while updating vehicle with VIN '" + vinToUpdate + "': " + e.getMessage());
            return CommandLine.ExitCode.SOFTWARE;
        }
    }
}
