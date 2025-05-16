package com.ev.apiclientjava.command;

import com.ev.apiclientjava.util.HttpClientUtil;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Callable;

/**
 * Picocli command to get (read) an electric vehicle record by its VIN from the API.
 */
@Command(name = "get",
        description = "Get an electric vehicle by its Vehicle Identification Number (VIN).",
        mixinStandardHelpOptions = true)
public class GetVehicleCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The VIN of the vehicle to retrieve.", arity = "1")
    private String vin;

    @Option(names = {"--api-base-url"},
            description = "Base URL of the Electric Vehicle API.",
            defaultValue = "http://localhost:8080/api/v1", // Default API URL
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS) // Show default in help message
    private String apiBaseUrl;

    /**
     * Executes the get vehicle command.
     * @return Exit code (0 for success, non-zero for failure).
     */
    @Override
    public Integer call() {
        String targetUrl = apiBaseUrl + "/vehicles/" + vin; // Construct the target URL

        // Build the HTTP GET request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Accept", "application/json") // Specify expected response format
                .GET()
                .build();
        try {
            // Send the request using the utility method
            HttpResponse<String> response = HttpClientUtil.sendRequest(request);
            // Return success if HTTP status is 200 OK, otherwise indicate an error
            return (response.statusCode() == 200) ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
        } catch (Exception e) {
            System.err.println("An error occurred while fetching vehicle with VIN '" + vin + "': " + e.getMessage());
            // For more detailed debugging, you might print the stack trace:
            // e.printStackTrace(System.err);
            return CommandLine.ExitCode.SOFTWARE; // Indicate a software error
        }
    }
}
