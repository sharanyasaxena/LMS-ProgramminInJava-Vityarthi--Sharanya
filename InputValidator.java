package com.library.util;

import java.util.regex.Pattern;

/**
 * Centralised input-validation rules used throughout the application.
 * Keeping validation in one place makes the rules easy to test and
 * reuse across the Book, Member and Transaction workflows.
 */
public class InputValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern ISBN_PATTERN = Pattern.compile("^[A-Za-z0-9-]{5,20}$");

    private InputValidator() {
        // utility class - no instances
    }

    public static boolean isNonEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return isNonEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return isNonEmpty(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidIsbn(String isbn) {
        return isNonEmpty(isbn) && ISBN_PATTERN.matcher(isbn).matches();
    }

    public static boolean isPositiveInteger(String value) {
        try {
            return Integer.parseInt(value.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNonNegativeInteger(String value) {
        try {
            return Integer.parseInt(value.trim()) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
