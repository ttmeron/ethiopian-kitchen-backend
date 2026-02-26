package com.resturant.entity;

public enum OrderStatus {
    CANCELLED,
    DELIVERED,
    READY,
    PROCESSING,
    PENDING,
    CONFIRMED,
    COMPLETED;

    public static OrderStatus fromString(String value) {
        if (value == null) return PROCESSING;
        try {
            return OrderStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PROCESSING;
        }
    }
}
