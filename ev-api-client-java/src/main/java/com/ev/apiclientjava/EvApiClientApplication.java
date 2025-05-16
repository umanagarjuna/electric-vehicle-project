package com.ev.apiclientjava;

import com.ev.apiclientjava.command.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import java.util.concurrent.Callable;

/**
 * Main application class for the Electric Vehicle Command Line Interface (CLI) client.
 * Uses Picocli to define commands and subcommands for interacting with the EV API.
 */
@Command(name = "ev-cli",
        mixinStandardHelpOptions = true, // Adds --help and --version options automatically
        versionProvider = EvApiClientApplication.ManifestVersionProvider.class,
        description = "Electric Vehicle API Client. Manages electric vehicle population data via a REST API.",
        subcommands = {
                GetVehicleCommand.class,
                CreateVehicleCommand.class,
                ListVehiclesCommand.class,
                UpdateVehicleCommand.class,
                DeleteVehicleCommand.class,
                UpdateMsrpBatchCommand.class
        })
public class EvApiClientApplication implements Callable<Integer> {

    /**
     * This method is called if no subcommand is specified.
     * It prints a usage message to standard error.
     * @return Exit code indicating usage error.
     */
    @Override
    public Integer call() {
        System.err.println("Please specify a command. Use '--help' for a list of available commands.");
        return CommandLine.ExitCode.USAGE; // Standard Picocli exit code for usage errors
    }

    /**
     * Main entry point for the CLI application.
     * Executes the command specified by the arguments.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new EvApiClientApplication())
                .setExecutionExceptionHandler((ex, commandLine, parseResult) -> {
                    // Custom exception handling for cleaner error messages to the user
                    System.err.println("Error: " + ex.getMessage());
                    // For detailed debugging, one might want to print the stack trace:
                    // ex.printStackTrace(System.err);
                    return CommandLine.ExitCode.SOFTWARE; // Standard Picocli exit code for internal software errors
                })
                .execute(args);
        System.exit(exitCode);
    }

    /**
     * Inner class to provide version information from the MANIFEST.MF file.
     * This is used by Picocli's --version option.
     */
    static class ManifestVersionProvider implements IVersionProvider {
        public String[] getVersion() {
            String version = EvApiClientApplication.class.getPackage().getImplementationVersion();
            return new String[] { version == null ? "Version not available in MANIFEST.MF" : version };
        }
    }
}