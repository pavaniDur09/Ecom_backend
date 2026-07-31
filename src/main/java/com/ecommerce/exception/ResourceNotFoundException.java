package com.ecommerce.exception;

/** Thrown when a requested entity (product, order, user, ...) does not exist. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
