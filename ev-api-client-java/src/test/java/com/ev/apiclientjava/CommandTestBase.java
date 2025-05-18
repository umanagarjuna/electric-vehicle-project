package com.ev.apiclientjava;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@ExtendWith(MockitoExtension.class)
public abstract class CommandTestBase {

    @Mock(lenient = true)  // Added lenient=true to prevent UnnecessaryStubbingException
    protected HttpResponse<String> mockResponse; // Mock for the response object

    protected final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    protected final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setUp() throws Exception {
        outContent.reset();
        errContent.reset();
        System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(errContent, true, StandardCharsets.UTF_8));
    }

    // This method should be called by tearDown in subclasses
    public void restoreStreamsBase() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
}