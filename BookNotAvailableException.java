package com.library.exception;

/** Thrown when every copy of a requested book is already issued out. */
public class BookNotAvailableException extends Exception {
    public BookNotAvailableException(String message) {
        super(message);
    }
}
