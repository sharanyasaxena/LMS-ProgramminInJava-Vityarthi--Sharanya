package com.library.exception;

/** Thrown when a lookup is attempted for an ISBN that does not exist in the catalogue. */
public class BookNotFoundException extends Exception {
    public BookNotFoundException(String message) {
        super(message);
    }
}
