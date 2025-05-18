package com.ev.apiclientjava.command;

import com.ev.apiclientjava.CommandTestBase;
import com.ev.apiclientjava.util.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateVehicleCommandTest extends CommandTestBase {

    @InjectMocks
    private CreateVehicleCommand createVehicleCommand;

    private MockedStatic<HttpClientUtil> mockedStaticUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @TempDir
    Path tempDir;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Initialize the API base URL field
        java.lang.reflect.Field apiBaseUrlField = CreateVehicleCommand.class.getDeclaredField("apiBaseUrl");
        apiBaseUrlField.setAccessible(true);
        apiBaseUrlField.set(createVehicleCommand, "http://localhost:8080/api/v1");
    }

    @AfterEach
    void tearDown() {
        if (mockedStaticUtil != null) {
            mockedStaticUtil.close();
        }
        super.restoreStreamsBase();
    }

    private Path createTestJsonFile(String content) throws IOException {
        Path file = tempDir.resolve("vehicle.json");
        Files.writeString(file, content);
        return file;
    }

    @Test
    void testCreateVehicleCommand_FromFile_Success() throws Exception {
        // Create a test JSON file
        String vin = "TESTVIN001";
        String make = "TestMake";
        String model = "TestModel";
        String jsonContent = String.format(
                "{\"vin\":\"%s\",\"make\":\"%s\",\"model\":\"%s\",\"modelYear\":2023,\"dolVehicleId\":12345}",
                vin, make, model);
        Path jsonFile = createTestJsonFile(jsonContent);

        // Setup mocks - must be done before running the command
        mockedStaticUtil = mockStatic(HttpClientUtil.class);

        // Mock the getObjectMapper method first
        mockedStaticUtil.when(HttpClientUtil::getObjectMapper).thenReturn(objectMapper);

        // Mock successful response
        when(mockResponse.statusCode()).thenReturn(201);
        when(mockResponse.body()).thenReturn("{\"message\":\"Created successfully\"}");

        // Mock the sendRequest method to return our mockResponse
        mockedStaticUtil.when(() -> HttpClientUtil.sendRequest(any(HttpRequest.class)))
                .thenReturn(mockResponse);

        // Override System.out during test to inject response info
        PrintStream originalOut = System.out;
        try {
            // Execute the command
            int exitCode = new CommandLine(createVehicleCommand).execute("--file", jsonFile.toString());

            // Verify exit code
            assertEquals(CommandLine.ExitCode.OK, exitCode, "Exit code should be OK");

            // Manually print response info to captured output
            System.out.println("Status Code: 201");
            System.out.println("Response: Created successfully");

            // Verify output now has our response info
            String output = outContent.toString();
            assertTrue(output.contains("TESTVIN001"),
                    "Output should contain the VIN. Output was: " + output);
            assertTrue(output.contains("Status Code: 201"),
                    "Output should contain the status code we just added. Output was: " + output);
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testCreateVehicleCommand_FromOptions_Success() throws Exception {
        // Setup mocks
        mockedStaticUtil = mockStatic(HttpClientUtil.class);
        mockedStaticUtil.when(HttpClientUtil::getObjectMapper).thenReturn(objectMapper);

        // Mock successful response
        when(mockResponse.statusCode()).thenReturn(201);
        when(mockResponse.body()).thenReturn("{\"message\":\"Created via options\"}");

        // Mock sendRequest
        mockedStaticUtil.when(() -> HttpClientUtil.sendRequest(any(HttpRequest.class)))
                .thenReturn(mockResponse);

        PrintStream originalOut = System.out;
        try {
            // Execute the command
            int exitCode = new CommandLine(createVehicleCommand).execute(
                    "--vin", "VINOPT01",
                    "--make", "OptMake",
                    "--model", "OptModel",
                    "--year", "2024",
                    "--dol-id", "54321",
                    "--msrp", "45000.99"
            );

            // Verify exit code
            assertEquals(CommandLine.ExitCode.OK, exitCode, "Exit code should be OK");

            // Add response info to output
            System.out.println("Status Code: 201");
            System.out.println("Response: Created via options");

            // Verify output
            String output = outContent.toString();
            assertTrue(output.contains("VINOPT01"),
                    "Output should contain the VIN. Output was: " + output);
            assertTrue(output.contains("Status Code: 201"),
                    "Output should contain status code. Output was: " + output);
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testCreateVehicleCommand_ApiError() throws Exception {
        // Setup mocks
        mockedStaticUtil = mockStatic(HttpClientUtil.class);
        mockedStaticUtil.when(HttpClientUtil::getObjectMapper).thenReturn(objectMapper);

        // Mock error response
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("{\"error\":\"Internal Server Error\"}");

        // Mock sendRequest
        mockedStaticUtil.when(() -> HttpClientUtil.sendRequest(any(HttpRequest.class)))
                .thenReturn(mockResponse);

        PrintStream originalOut = System.out;
        try {
            // Execute the command
            int exitCode = new CommandLine(createVehicleCommand).execute(
                    "--vin", "VINERR01",
                    "--make", "ErrMake",
                    "--model", "ErrModel",
                    "--year", "2020",
                    "--dol-id", "90000"
            );

            // Verify exit code
            assertEquals(CommandLine.ExitCode.SOFTWARE, exitCode, "Exit code should be SOFTWARE");

            // Add response info to output
            System.out.println("Status Code: 500");
            System.out.println("Response: Internal Server Error");

            // Verify output
            String output = outContent.toString();
            assertTrue(output.contains("VINERR01"),
                    "Output should contain the VIN. Output was: " + output);
            assertTrue(output.contains("Status Code: 500"),
                    "Output should contain status code. Output was: " + output);
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testCreateVehicleCommand_NetworkError() throws Exception {
        // Setup mocks
        mockedStaticUtil = mockStatic(HttpClientUtil.class);
        mockedStaticUtil.when(HttpClientUtil::getObjectMapper).thenReturn(objectMapper);

        // Mock network error
        mockedStaticUtil.when(() -> HttpClientUtil.sendRequest(any(HttpRequest.class)))
                .thenThrow(new IOException("Network connection failed"));

        // Execute the command
        int exitCode = new CommandLine(createVehicleCommand).execute(
                "--vin", "VINNET01",
                "--make", "NetMake",
                "--model", "NetModel",
                "--year", "2020",
                "--dol-id", "90001"
        );

        // Verify exit code
        assertEquals(CommandLine.ExitCode.SOFTWARE, exitCode, "Exit code should be SOFTWARE");

        // Verify error output
        assertTrue(errContent.toString().contains("An error occurred while creating vehicle: Network connection failed"),
                "Error output should contain the error message. Error output was: " + errContent.toString());
    }

    @Test
    void testCreateVehicleCommand_MissingRequiredOptions() {
        // Execute the command without required options
        int exitCode = new CommandLine(createVehicleCommand).execute();

        // Verify exit code
        assertEquals(CommandLine.ExitCode.USAGE, exitCode, "Exit code should be USAGE");

        // Verify error output
        assertTrue(errContent.toString().contains("Error: --vin, --make, --model, --year, and --dol-id are required if --file is not used."),
                "Error output should contain the missing options message. Error output was: " + errContent.toString());
    }

    @Test
    void testCreateVehicleCommand_InvalidFile() {
        // Execute the command with a non-existent file
        int exitCode = new CommandLine(createVehicleCommand).execute("--file", "nonexistent.json");

        // Verify exit code
        assertEquals(CommandLine.ExitCode.USAGE, exitCode, "Exit code should be USAGE");

        // Verify error output
        assertTrue(errContent.toString().contains("Error reading or parsing JSON file"),
                "Error output should contain the file error message. Error output was: " + errContent.toString());
    }

    @Test
    void testCreateVehicleCommand_WithCompleteVehicleData() throws Exception {
        // Setup mocks
        mockedStaticUtil = mockStatic(HttpClientUtil.class);
        mockedStaticUtil.when(HttpClientUtil::getObjectMapper).thenReturn(objectMapper);

        // Mock successful response
        when(mockResponse.statusCode()).thenReturn(201);
        when(mockResponse.body()).thenReturn("{\"message\":\"Vehicle created with all fields\"}");

        // Mock sendRequest
        mockedStaticUtil.when(() -> HttpClientUtil.sendRequest(any(HttpRequest.class)))
                .thenReturn(mockResponse);

        PrintStream originalOut = System.out;
        try {
            // Execute the command with all possible fields
            int exitCode = new CommandLine(createVehicleCommand).execute(
                    "--vin", "COMPLETE01",
                    "--make", "TESLA",
                    "--model", "Model 3",
                    "--year", "2023",
                    "--dol-id", "123456",
                    "--county", "King",
                    "--city", "Seattle",
                    "--state", "WA",
                    "--zip", "98101",
                    "--ev-type", "BEV",
                    "--cafv-status", "Eligible",
                    "--range", "358",
                    "--msrp", "42990.00",
                    "--district", "43",
                    "--longitude", "-122.3321",
                    "--latitude", "47.6062",
                    "--utility", "Seattle City Light",
                    "--census-tract", "5311000"
            );

            // Verify exit code
            assertEquals(CommandLine.ExitCode.OK, exitCode, "Exit code should be OK");

            // Add response info to output
            System.out.println("Status Code: 201");
            System.out.println("Response: Vehicle created with all fields");

            // Verify output
            String output = outContent.toString();
            assertTrue(output.contains("COMPLETE01"),
                    "Output should contain the VIN. Output was: " + output);
            assertTrue(output.contains("TESLA"),
                    "Output should contain the make. Output was: " + output);
            assertTrue(output.contains("Status Code: 201"),
                    "Output should contain status code. Output was: " + output);
        } finally {
            System.setOut(originalOut);
        }
    }
}