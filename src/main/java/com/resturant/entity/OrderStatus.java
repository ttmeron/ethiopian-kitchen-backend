package com.resturant.entity;

public enum OrderStatus {
    CANCELLED,
    DELIVERED,
    READY,
    PROCESSING,
    COMPLETED;

    public static OrderStatus fromString(String value) {
        if (value == null) return PROCESSING; // Default value
        try {
            return OrderStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PROCESSING; // Fallback to default
        }
    }
}
