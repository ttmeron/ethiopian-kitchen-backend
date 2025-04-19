package com.resturant.entity;

public enum OrderStatus {
    PENDING,
    COMPLETED,
    CANCELLED,
    PROCESSING;

    public static OrderStatus fromString(String value) {
        if (value == null) return PENDING; // Default value
        try {
            return OrderStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING; // Fallback to default
        }
    }
}
