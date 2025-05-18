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
 * Picocli command to delete an electric vehicle record by its VIN via the API.
 */
@Command(name = "delete",
        description = "Delete an electric vehicle record by its Vehicle Identification Number (VIN).",
        mixinStandardHelpOptions = true)
public class DeleteVehicleCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The VIN of the vehicle to delete.", arity = "1")
    private String vinToDelete;

    @Option(names = {"--api-base-url"}, description = "Base URL of the EV API.", defaultValue = "http://localhost:8080/api/v1", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private String apiBaseUrl;

    @Override
    public Integer call() {
        String targetUrl = apiBaseUrl + "/vehicles/" + vinToDelete;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Accept", "application/json") // Though DELETE might not return JSON, it's good practice
                .DELETE() // HTTP DELETE method
                .build();
        try {
            HttpResponse<String> response = HttpClientUtil.sendRequest(request);
            // HTTP 204 No Content indicates successful deletion
            if (response.statusCode() == 204) {
                System.out.println("Vehicle with VIN '" + vinToDelete + "' deleted successfully.");
                return CommandLine.ExitCode.OK;
            } else {
                // Error details are printed by HttpClientUtil.handleResponseOutput
                // This part is reached if status code is not 204 (e.g., 404 Not Found)
                System.err.println("Failed to delete vehicle. API responded with status: " + response.statusCode());
                return CommandLine.ExitCode.SOFTWARE;
            }
        } catch (Exception e) {
            System.err.println("An error occurred while deleting vehicle with VIN '" + vinToDelete + "': " + e.getMessage());
            return CommandLine.ExitCode.SOFTWARE;
        }
    }
}
