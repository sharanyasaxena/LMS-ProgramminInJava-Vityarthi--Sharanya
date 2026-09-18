package com.library.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic helper for reading and writing plain-text CSV files.
 * This is the persistence layer of the application: since the course
 * scope does not require a database server, flat files under data/
 * act as lightweight, human-readable storage that survives restarts.
 */
public class FileStorage {

    /** Ensures the data directory exists before any read/write happens. */
    public static void ensureDataDirectory(String dirPath) {
        try {
            Path path = Paths.get(dirPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            Logger.error("Could not create data directory: " + e.getMessage());
        }
    }

    /** Reads every line of a file. Returns an empty list if the file does not yet exist. */
    public static List<String> readLines(String filePath) {
        List<String> lines = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return lines;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            Logger.error("Failed to read " + filePath + ": " + e.getMessage());
        }
        return lines;
    }

    /** Overwrites a file with the given list of lines (one record per line). */
    public static void writeLines(String filePath, List<String> lines) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, false))) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            Logger.error("Failed to write " + filePath + ": " + e.getMessage());
        }
    }
}
