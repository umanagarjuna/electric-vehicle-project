package com.ev.apiclientjava.command;

import com.ev.apiclientjava.util.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Picocli command to perform a batch update of Base MSRP for vehicles
 * of a specific make and model via the API.
 */
@Command(name = "update-msrp-batch",
        description = "Update Base MSRP for all vehicles of a specific make and model.",
        mixinStandardHelpOptions = true)
public class UpdateMsrpBatchCommand implements Callable<Integer> {

    @Option(names = {"--api-base-url"}, description = "Base URL of the EV API.",
            defaultValue = "http://localhost:8080/api/v1", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private String apiBaseUrl;

    @Option(names = "--make", required = true, description = "Make of the vehicles (e.g., TESLA)")
    private String make;

    @Option(names = "--model", required = true, description = "Model of the vehicles (e.g., Model Y)")
    private String model;

    @Option(names = "--new-msrp", required = true, description = "The new Base MSRP value to set (e.g., 75990.00).")
    private BigDecimal newBaseMSRP;

    private final ObjectMapper objectMapper = HttpClientUtil.getObjectMapper();

    @Override
    public Integer call() throws Exception {
        String targetUrl = apiBaseUrl + "/vehicles/batch/msrp"; // Endpoint for batch MSRP update

        // Prepare the request payload
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("make", make);
        requestPayload.put("model", model);
        requestPayload.put("newBaseMSRP", newBaseMSRP);

        String requestBody = objectMapper.writeValueAsString(requestPayload);
        // Log the request body being sent for debugging purposes
        System.out.println("Request Body for Batch MSRP Update:\n" + requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody)) // HTTP PATCH method
                .build();
        try {
            HttpResponse<String> response = HttpClientUtil.sendRequest(request);
            // HTTP 200 OK indicates success for this batch operation
            return (response.statusCode() == 200) ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
        } catch (Exception e) {
            System.err.println("An error occurred while batch updating MSRP for make '" + make + "' and model '"
                    + model + "': " + e.getMessage());
            return CommandLine.ExitCode.SOFTWARE;
        }
    }
}
