package com.ecommerce.exception;

/** Thrown when payment processing fails for an order. */
public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) {
        super(message);
    }
}
