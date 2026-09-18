package com.library.exception;

/** Thrown when a lookup is attempted for a member ID that does not exist. */
public class MemberNotFoundException extends Exception {
    public MemberNotFoundException(String message) {
        super(message);
    }
}
