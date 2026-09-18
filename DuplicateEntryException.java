package com.library.exception;

/** Thrown when trying to insert a book/member ID that already exists,
 *  or when a business rule (e.g. borrowing limit) is violated. */
public class DuplicateEntryException extends Exception {
    public DuplicateEntryException(String message) {
        super(message);
    }
}
