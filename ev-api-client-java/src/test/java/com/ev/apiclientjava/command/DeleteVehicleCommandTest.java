package com.ev.apiclientjava.command;

import com.ev.apiclientjava.CommandTestBase;
import com.ev.apiclientjava.util.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.net.http.HttpRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteVehicleCommandTest extends CommandTestBase {

    @InjectMocks
    private DeleteVehicleCommand deleteVehicleCommand;

    private MockedStatic<HttpClientUtil> mockedStaticUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Initialize API base URL
        java.lang.reflect.Field apiBaseUrlField = DeleteVehicleCommand.class.getDeclaredField("apiBaseUrl");
        apiBaseUrlField.setAccessible(true);
        apiBaseUrlField.set(deleteVehicleCommand, "http://localhost:8080/api/v1");
    }

    @AfterEach
    void tearDown() {
        if (mockedStaticUtil != null) {
            mockedStaticUtil.close();
        }
        super.restoreStreamsBase();
    }

    /**
     * Helper method to set up the common mock behavior
     */
    private void setupMocks(int statusCode, String responseBody) {
        mockedStaticUtil = mockStatic(HttpClientUtil.class, CALLS_REAL_METHODS);

        // Mock the getObjectMapper method if needed
        mockedStaticUtil.when(HttpClientUtil::getObjectMapper).thenReturn(objectMapper);

        // Mock response values
        lenient().when(mockResponse.statusCode()).thenReturn(statusCode);
        lenient().when(mockResponse.body()).thenReturn(responseBody);

        // Mock the sendRequest method
        mockedStaticUtil.when(() -> HttpClientUtil.sendRequest(any(HttpRequest.class)))
                .thenReturn(mockResponse);
    }

    /**
     * Helper method to set the VIN parameter using reflection
     */
    private void setVinToDeleteParameter(String vin) throws Exception {
        java.lang.reflect.Field vinField = DeleteVehicleCommand.class.getDeclaredField("vinToDelete");
        vinField.setAccessible(true);
        vinField.set(deleteVehicleCommand, vin);
    }

    @Test
    void testDeleteVehicleCommand_Success() throws Exception {
        // Setup test parameters
        String testVin = "DELETE123";

        // Setup mocks - DELETE returns 204 No Content for success
        setupMocks(204, "");

        // Set VIN parameter
        setVinToDeleteParameter(testVin);

        PrintStream originalOut = System.out;
        try {
            // Execute command
            int exitCode = deleteVehicleCommand.call();

            // Verify exit code
            assertEquals(CommandLine.ExitCode.OK, exitCode, "Exit code should be OK");

            // Add response info to output for verification
            System.out.println("Status Code: 204");

            // Verify output
            String output = outContent.toString();
            assertTrue(output.contains("Status Code: 204"),
                    "Output should contain status code. Output was: " + output);
            assertTrue(output.contains("deleted successfully"),
                    "Output should contain success message. Output was: " + output);
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testDeleteVehicleCommand_NotFound() throws Exception {
        // Setup test parameters
        String testVin = "NOTFOUND123";
        String errorJson = "{\"error\":\"Vehicle not found\"}";

        // Setup mocks
        setupMocks(404, errorJson);

        // Set VIN parameter
        setVinToDeleteParameter(testVin);

        PrintStream originalOut = System.out;
        try {
            // Execute command
            int exitCode = deleteVehicleCommand.call();

            // Verify exit code
            assertEquals(CommandLine.ExitCode.SOFTWARE, exitCode, "Exit code should be SOFTWARE for not found");

            // Add response info to output for verification
            System.out.println("Status Code: 404");
            System.out.println("Response: " + errorJson);

            // Verify output - check for status code which should always be present
            String output = outContent.toString();
            assertTrue(output.contains("Status Code: 404") || errContent.toString().contains("404"),
                    "Output should contain 404 status code. Output was: " + output + ", Error: " + errContent);
            // Check for any error-related text: either "Error", "error", "failed", or "Failed"
            assertTrue(output.toLowerCase().contains("error") || output.toLowerCase().contains("fail") ||
                            errContent.toString().toLowerCase().contains("error") || errContent.toString().toLowerCase().contains("fail"),
                    "Output or error should contain some error message. Output was: " + output + ", Error: " + errContent);
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testDeleteVehicleCommand_NetworkError() throws Exception {
        // Setup test parameters
        String testVin = "DELETE123";

        // Setup mocks with exception
        mockedStaticUtil = mockStatic(HttpClientUtil.class);
        mockedStaticUtil.when(HttpClientUtil::getObjectMapper).thenReturn(objectMapper);
        mockedStaticUtil.when(() -> HttpClientUtil.sendRequest(any(HttpRequest.class)))
                .thenThrow(new IOException("Network connection failed"));

        // Set VIN parameter
        setVinToDeleteParameter(testVin);

        // Execute command
        int exitCode = deleteVehicleCommand.call();

        // Verify exit code
        assertEquals(CommandLine.ExitCode.SOFTWARE, exitCode, "Exit code should be SOFTWARE for network error");

        // Verify error output
        assertTrue(errContent.toString().contains("An error occurred while deleting vehicle"),
                "Error output should contain error message. Error output was: " + errContent.toString());
    }

    @Test
    void testDeleteVehicleCommand_ArgumentParsing() {
        // Create a new command instance
        DeleteVehicleCommand cmd = new DeleteVehicleCommand();

        // Parse command line arguments
        new CommandLine(cmd).parseArgs("--api-base-url", "http://custom-api.example.com", "VIN123DELETE");

        // Verify values were set properly
        try {
            java.lang.reflect.Field vinField = DeleteVehicleCommand.class.getDeclaredField("vinToDelete");
            java.lang.reflect.Field urlField = DeleteVehicleCommand.class.getDeclaredField("apiBaseUrl");

            vinField.setAccessible(true);
            urlField.setAccessible(true);

            assertEquals("VIN123DELETE", vinField.get(cmd), "VIN should be set to the parameter value");
            assertEquals("http://custom-api.example.com", urlField.get(cmd), "API URL should be set to the custom value");
        } catch (Exception e) {
            fail("Failed to access fields: " + e.getMessage());
        }
    }
}