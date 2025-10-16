package com.resturant.dto;

import com.resturant.entity.OrderStatus;
import com.resturant.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Order information with items and delivery details")
public class OrderDTO {

    @Schema(
            description = "Unique identifier of the order",
            example = "1001",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long orderId;

    @Schema(
            description = "When the order was placed",
            example = "2024-01-15T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String placedTime;

    @Schema(
            description = "Total price of the order in Ethiopian Birr",
            example = "750.50",
            required = true
    )
    private BigDecimal totalPrice;
    @Schema(
            description = "Current status of the order",
            example = "PROCESSING",
            allowableValues = {"NEW", "PROCESSING", "DELIVERED", "CANCELLED"}
    )
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    @Schema(description = "Special instructions for order preparation",
            example = "No onions, extra spicy")
    private String specialInstructions;
    @Schema(
            description = "Name of the customer placing the order",
            example = "Abebe Kebede",
            required = true
    )
    @NotNull
    private String userName;
    @Schema(
            description = "Email address of the customer",
            example = "meron@example.com",
            format = "email"
    )
    @Email
    private String email;


    @ArraySchema(
            arraySchema = @Schema(
                    description = "List of items in the order",
                    required = true
            ),
            schema = @Schema(implementation = OrderItemDTO.class)
    )
    @Valid
    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemDTO> orderItems;
    @Schema(
            description = "Delivery information for the order"
    )
    private DeliveryDTO deliveryDTO;

    private boolean isGuest;


    private String trackingToken;

    private String remainingTime;
}
