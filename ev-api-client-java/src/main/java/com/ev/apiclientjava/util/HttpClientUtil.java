package com.ev.apiclientjava.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // For Java 8 Date/Time

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Utility class for handling HTTP client operations and JSON processing.
 */
public class HttpClientUtil {

    // Reusable HttpClient instance with default settings
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1) // Prefer HTTP/1.1 or HTTP_2 if server supports
            .followRedirects(HttpClient.Redirect.NORMAL) // Handle redirects automatically
            .build();

    // Reusable ObjectMapper for JSON serialization/deserialization
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT) // Pretty print JSON output
            .registerModule(new JavaTimeModule()) // Support for Java 8 Date/Time types
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Write dates in ISO-8601 format

    /**
     * Sends an HTTP request and handles basic logging of the request and response.
     * @param request The HttpRequest to send.
     * @return The HttpResponse received from the server.
     * @throws Exception If an error occurs during sending or receiving.
     */
    public static HttpResponse<String> sendRequest(HttpRequest request) throws Exception {
        System.out.println(">>> Sending " + request.method() + " request to: " + request.uri());
        // Note: Logging the actual request body for POST/PUT can be complex
        // as BodyPublishers are often stream-based and can only be consumed once.
        // For simple StringBodyPublishers, you could potentially log it, but it's omitted here for generality.

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("<<< Received response:");
        handleResponseOutput(response); // Process and print response details
        return response;
    }

    /**
     * Prints the status code and body of an HTTP response.
     * Attempts to pretty-print the body if it's JSON.
     * @param response The HttpResponse to handle.
     */
    public static void handleResponseOutput(HttpResponse<String> response) {
        System.out.println("Status Code: " + response.statusCode());
        String body = response.body();
        if (body != null && !body.isEmpty()) {
            // Check content type to decide if pretty printing JSON is appropriate
            String contentType = response.headers().firstValue("Content-Type").orElse("").toLowerCase();
            if (contentType.contains("application/json")) {
                try {
                    Object jsonResponse = objectMapper.readValue(body, Object.class); // Parse as generic JSON
                    System.out.println("Response Body (JSON):");
                    System.out.println(objectMapper.writeValueAsString(jsonResponse)); // Pretty print
                } catch (JsonProcessingException e) {
                    // If parsing as JSON fails, print as plain text
                    System.out.println("Response Body (Could not parse as JSON, printing as text):");
                    System.out.println(body);
                }
            } else {
                System.out.println("Response Body (Content-Type: " + contentType + "):");
                System.out.println(body);
            }
        } else {
            System.out.println("(No content in response body)");
        }
    }

    /**
     * Provides access to the shared ObjectMapper instance.
     * @return The configured ObjectMapper.
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
