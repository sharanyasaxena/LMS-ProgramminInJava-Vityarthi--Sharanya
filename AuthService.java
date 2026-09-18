package com.library.service;

import com.library.util.FileStorage;
import com.library.util.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Handles librarian/admin authentication.
 * Passwords are never stored or compared in plain text: they are hashed
 * with SHA-256 before being written to disk (addresses the "Security"
 * non-functional requirement). On first run a default admin account
 * (admin / admin123) is created automatically.
 */
public class AuthService {

    private static final String CREDENTIALS_FILE = "data/admin.csv";
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin123";

    public AuthService() {
        seedDefaultAdminIfMissing();
    }

    private void seedDefaultAdminIfMissing() {
        List<String> lines = FileStorage.readLines(CREDENTIALS_FILE);
        if (lines.isEmpty()) {
            String hashed = hash(DEFAULT_PASSWORD);
            FileStorage.writeLines(CREDENTIALS_FILE, List.of(DEFAULT_USERNAME + "," + hashed));
            Logger.info("Default admin account created (username: admin, password: admin123)");
        }
    }

    /** Verifies a login attempt against the stored, hashed credentials. */
    public boolean login(String username, String password) {
        List<String> lines = FileStorage.readLines(CREDENTIALS_FILE);
        String hashedAttempt = hash(password);
        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(hashedAttempt)) {
                Logger.info("Successful login for user: " + username);
                return true;
            }
        }
        Logger.warn("Failed login attempt for user: " + username);
        return false;
    }

    /** Allows the admin to change their password (also hashed before saving). */
    public boolean changePassword(String username, String newPassword) {
        List<String> lines = FileStorage.readLines(CREDENTIALS_FILE);
        boolean updated = false;
        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",", -1);
            if (parts.length == 2 && parts[0].equals(username)) {
                lines.set(i, username + "," + hash(newPassword));
                updated = true;
                break;
            }
        }
        if (updated) {
            FileStorage.writeLines(CREDENTIALS_FILE, lines);
            Logger.info("Password changed for user: " + username);
        }
        return updated;
    }

    private String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be present on every standard JVM.
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
