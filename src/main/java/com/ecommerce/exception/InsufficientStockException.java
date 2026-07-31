package com.ecommerce.exception;

/** Thrown when an order tries to reserve more stock than is available. */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
