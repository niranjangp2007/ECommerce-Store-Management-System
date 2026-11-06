package com.ecommerce.service;

/**
 * Custom runtime/checked exception for user-friendly database operational errors.
 */
public class DatabaseOperationException extends Exception {

    public DatabaseOperationException(String message) {
        super(message);
    }

    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
