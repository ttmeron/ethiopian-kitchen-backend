package com.resturant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Delivery information for an order")
public class DeliveryDTO {

    @Schema(
            description = "Unique identifier of the delivery",
            example = "101"
    )
    private Long id;
    @Schema(
            description = "ID of the associated order",
            example = "5001"
    )
    private Long orderId;
    @Schema(
            description = "Full delivery address",
            example = "Bole Road, Addis Ababa, Ethiopia"
    )
    private String deliveryAddress;
    @Schema(
            description = "Scheduled delivery date and time",
            example = "2025-04-25T18:30:00"
    )
    private LocalDateTime deliveryTime;
    @Schema(
            description = "Current delivery status",
            example = "SCHEDULED",
            allowableValues = {"SCHEDULED", "IN_TRANSIT", "DELIVERED", "CANCELLED"}
    )
    private String status;
}
