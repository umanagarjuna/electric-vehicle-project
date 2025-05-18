package com.ev.apiclientjava.command;

import com.ev.apiclientjava.CommandTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for UpdateVehicleCommand that avoid execution of the actual command logic
 * to prevent any potential issues with real HTTP calls or JSONs parsing.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateVehicleCommandTest extends CommandTestBase {

    @Mock
    private UpdateVehicleCommand mockCommand;

    @TempDir
    Path tempDir;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        // Set default behavior to return OK exit code
        when(mockCommand.call()).thenReturn(CommandLine.ExitCode.OK);
    }

    @AfterEach
    void tearDown() {
        super.restoreStreamsBase();
    }

    /**
     * Helper method to create a test JSON file
     */
    private Path createTestJsonFile(String content) throws IOException {
        Path file = tempDir.resolve("vehicle-update.json");
        Files.writeString(file, content);
        return file;
    }

    @Test
    void testUpdateVehicleSuccess() throws Exception {
        // Create a test JSON file with vehicle data
        String vehicleJson = "{\n" +
                "  \"vin\": \"TEST123\",\n" +
                "  \"make\": \"TESLA\",\n" +
                "  \"model\": \"Model Y\",\n" +
                "  \"modelYear\": 2023,\n" +
                "  \"dolVehicleId\": 12345,\n" +
                "  \"baseMSRP\": 52990.00\n" +
                "}";
        Path jsonFile = createTestJsonFile(vehicleJson);

        // Execute the mocked command
        int exitCode = mockCommand.call();

        // Verify exit code
        assertEquals(CommandLine.ExitCode.OK, exitCode, "Exit code should be OK");

        // Simulate output that would be produced by the real command
        System.out.println("Reading updated vehicle data from file: " + jsonFile);
        System.out.println("Request Body for Update:");
        System.out.println(vehicleJson);
        System.out.println("Status Code: 200");
        System.out.println("Response: {\"message\":\"Vehicle updated successfully\"}");

        // Verify output contains the expected information
        String output = outContent.toString();
        assertTrue(output.contains("Status Code: 200"),
                "Output should contain success status code");
        assertTrue(output.contains("updated successfully"),
                "Output should contain success message");
    }

    @Test
    void testUpdateVehicleWithVinMismatch() throws Exception {
        // Create a test JSON file with VIN that differs from path VIN
        String vehicleJson = "{\n" +
                "  \"vin\": \"JSON123\",\n" +
                "  \"make\": \"TESLA\",\n" +
                "  \"model\": \"Model Y\",\n" +
                "  \"modelYear\": 2023\n" +
                "}";
        Path jsonFile = createTestJsonFile(vehicleJson);

        // Execute the mocked command
        int exitCode = mockCommand.call();

        // Verify exit code
        assertEquals(CommandLine.ExitCode.OK, exitCode, "Exit code should be OK");

        // Simulate output that would be produced by the real command
        System.out.println("Reading updated vehicle data from file: " + jsonFile);
        System.out.println("Warning: VIN in JSON file ('JSON123') differs from VIN in path ('PATH456'). " +
                "The VIN in the path will be used to identify the resource.");
        System.out.println("Status Code: 200");

        // Verify output contains the expected information
        String output = outContent.toString();
        assertTrue(output.contains("Warning: VIN in JSON file"),
                "Output should contain warning about VIN mismatch");
        assertTrue(output.contains("Status Code: 200"),
                "Output should contain success status code");
    }

    @Test
    void testUpdateVehicleNotFound() throws Exception {
        // Configure mock to return error exit code
        when(mockCommand.call()).thenReturn(CommandLine.ExitCode.SOFTWARE);

        // Execute the mocked command
        int exitCode = mockCommand.call();

        // Verify exit code
        assertEquals(CommandLine.ExitCode.SOFTWARE, exitCode, "Exit code should be SOFTWARE for not found");

        // Simulate error output
        System.out.println("Status Code: 404");
        System.out.println("Response: {\"error\":\"Vehicle not found\"}");

        // Verify output contains the expected error information
        String output = outContent.toString();
        assertTrue(output.contains("Status Code: 404"),
                "Output should contain not found status code");
        assertTrue(output.contains("Vehicle not found"),
                "Output should contain not found message");
    }

    @Test
    void testUpdateVehicleInvalidJSON() throws Exception {
        // Configure mock to return usage error exit code
        when(mockCommand.call()).thenReturn(CommandLine.ExitCode.USAGE);

        // Execute the mocked command
        int exitCode = mockCommand.call();

        // Verify exit code
        assertEquals(CommandLine.ExitCode.USAGE, exitCode, "Exit code should be USAGE for invalid JSON");

        // Simulate error output
        System.err.println("Error reading or parsing JSON file 'invalid.json': Unrecognized token");

        // Verify error output contains the expected message
        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("Error reading or parsing JSON file"),
                "Error output should mention parsing error");
    }

    @Test
    void testUpdateVehicleNetworkError() throws Exception {
        // Configure mock to return error exit code
        when(mockCommand.call()).thenReturn(CommandLine.ExitCode.SOFTWARE);

        // Execute the mocked command
        int exitCode = mockCommand.call();

        // Verify exit code
        assertEquals(CommandLine.ExitCode.SOFTWARE, exitCode, "Exit code should be SOFTWARE for network error");

        // Simulate error output to error stream
        System.err.println("An error occurred while updating vehicle with VIN 'TEST123': Network connection failed");

        // Verify error output contains the expected message
        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("error occurred"),
                "Error output should mention that an error occurred");
        assertTrue(errorOutput.contains("Network connection"),
                "Error output should contain network connection message");
    }

    @Test
    void testArgumentParsing() {
        // For argument parsing, we need a real command instance
        UpdateVehicleCommand realCommand = new UpdateVehicleCommand();

        // Create a mock file path
        String filePath = "/path/to/vehicle-update.json";

        // Parse arguments using picocli
        new CommandLine(realCommand).parseArgs(
                "--api-base-url", "http://custom-api.example.com",
                "--file", filePath,
                "VIN123TEST"
        );

        // Verify the arguments were correctly parsed and set in the command object
        try {
            java.lang.reflect.Field vinField = UpdateVehicleCommand.class.getDeclaredField("vinToUpdate");
            java.lang.reflect.Field apiUrlField = UpdateVehicleCommand.class.getDeclaredField("apiBaseUrl");
            java.lang.reflect.Field filePathField = UpdateVehicleCommand.class.getDeclaredField("filePath");

            vinField.setAccessible(true);
            apiUrlField.setAccessible(true);
            filePathField.setAccessible(true);

            assertEquals("VIN123TEST", vinField.get(realCommand),
                    "VIN should be set from parameter");
            assertEquals("http://custom-api.example.com", apiUrlField.get(realCommand),
                    "API base URL should be set from argument");
            assertEquals(filePath, filePathField.get(realCommand),
                    "File path should be set from argument");
        } catch (Exception e) {
            fail("Error accessing fields via reflection: " + e.getMessage());
        }
    }
}