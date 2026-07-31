package com.ecommerce.model.enums;

/**
 * Lifecycle states of an Order.
 * PENDING -> CONFIRMED -> SHIPPED -> DELIVERED
 *                     \-> CANCELLED
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
