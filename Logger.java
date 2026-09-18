package com.library.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A minimal, dependency-free logging utility.
 * Every significant action (login, issue, return, error) is appended
 * to data/app.log so the application's behaviour can be audited later
 * (addresses the "Logging / Monitoring" non-functional requirement).
 */
public class Logger {

    private static final String LOG_FILE = "data/app.log";
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Logger() {
        // utility class - no instances
    }

    public static void info(String message) {
        write("INFO", message);
    }

    public static void warn(String message) {
        write("WARN", message);
    }

    public static void error(String message) {
        write("ERROR", message);
    }

    private static void write(String level, String message) {
        String line = "[" + LocalDateTime.now().format(FORMAT) + "] " + level + " - " + message;
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println(line);
        } catch (IOException e) {
            // Logging must never crash the application; fall back to stderr.
            System.err.println("Could not write to log file: " + e.getMessage());
        }
    }
}
