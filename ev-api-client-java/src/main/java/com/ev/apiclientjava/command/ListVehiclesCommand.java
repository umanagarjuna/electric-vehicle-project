package com.ev.apiclientjava.command;

import com.ev.apiclientjava.dto.PaginatedVehicleResponse;
import com.ev.apiclientjava.util.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

/**
 * Picocli command to list electric vehicles from the API with pagination and sorting.
 */
@Command(name = "list",
        description = "List all electric vehicles with pagination and sorting.",
        mixinStandardHelpOptions = true)
public class ListVehiclesCommand implements Callable<Integer> {

    @Option(names = {"--api-base-url"}, description = "Base URL of the EV API.", defaultValue = "http://localhost:8080/api/v1", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private String apiBaseUrl;

    @Option(names = {"--page"}, description = "Page number to retrieve (0-indexed).", defaultValue = "0")
    private int page;

    @Option(names = {"--size"}, description = "Number of records per page.", defaultValue = "10")
    private int size;

    @Option(names = {"--sort"}, description = "Sort criteria (e.g., 'vin,asc' or 'modelYear,desc').", defaultValue = "vin,asc")
    private String sort;

    private final ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();

    @Override
    public Integer call() throws Exception {
        // Construct query parameters for pagination and sorting
        StringBuilder queryParams = new StringBuilder();
        queryParams.append("page=").append(page);
        queryParams.append("&size=").append(size);
        // URL encode the sort parameter to handle special characters like comma
        queryParams.append("&sort=").append(URLEncoder.encode(sort, StandardCharsets.UTF_8.name()));

        String targetUrl = apiBaseUrl + "/vehicles?" + queryParams.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = HttpClientUtil.sendRequest(request);
            if (response.statusCode() == 200) {
                // Deserialize the response into our PaginatedVehicleResponse DTO
                PaginatedVehicleResponse paginatedResponse = objectMapper.readValue(response.body(), PaginatedVehicleResponse.class);
                // HttpClientUtil.handleResponseOutput already prints the body.
                // Add some summary information.
                System.out.println(String.format("--- Summary: Page %d of %d. Total Vehicles: %d. Vehicles on this page: %d ---",
                        paginatedResponse.getNumber(), // API returns 0-indexed page number
                        paginatedResponse.getTotalPages(),
                        paginatedResponse.getTotalElements(),
                        paginatedResponse.getNumberOfElements()));
                return CommandLine.ExitCode.OK;
            } else {
                // Error details are printed by HttpClientUtil.handleResponseOutput
                return CommandLine.ExitCode.SOFTWARE;
            }
        } catch (Exception e) {
            System.err.println("An error occurred while listing vehicles: " + e.getMessage());
            return CommandLine.ExitCode.SOFTWARE;
        }
    }
}