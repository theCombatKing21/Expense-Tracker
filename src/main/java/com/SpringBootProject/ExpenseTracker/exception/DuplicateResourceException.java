package com.SpringBootProject.ExpenseTracker.exception;

// Thrown when someone tries to create a resource that already exists —
// for example, registering a User with an email that's already in the database,
// or creating a Category whose name is already taken.
// Maps to HTTP 409 Conflict in GlobalExceptionHandler.
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}