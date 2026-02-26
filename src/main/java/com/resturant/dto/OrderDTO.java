package com.resturant.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.resturant.entity.OrderStatus;
import com.resturant.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.AssertTrue;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder", builderMethodName = "builder", toBuilder = true)
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
    @JsonProperty("userName")
    private String userName;
    @Schema(
            description = "Email address of the customer",
            example = "meron@example.com",
            format = "email"
    )

    @JsonProperty("email")
    private String email;


    @ArraySchema(
            arraySchema = @Schema(
                    description = "List of items in the order",
                    required = true
            ),
            schema = @Schema(implementation = OrderItemDTO.class)
    )

    private List<OrderItemDTO> orderItems;
    @Schema(
            description = "Delivery information for the order"
    )
    private DeliveryDTO deliveryDTO;

    @Schema(
            description = "Whether this is a guest order",
            example = "false",
            defaultValue = "false"
    )
    @JsonProperty("isGuest")
    private boolean isGuest;


    @Schema(
            description = "Tracking token for order status updates",
            example = "TRK-ABC123XYZ",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String trackingToken;

    @Schema(
            description = "Estimated remaining time for order completion",
            example = "25 minutes",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String remainingTime;

    @Schema(
            description = "Guest email (for guest orders only)",
            example = "guest@example.com"
    )
    private String guestEmail;

    @Schema(
            description = "Guest name (for guest orders only)",
            example = "Guest User"
    )
    private String guestName;

    @Schema(
            description = "Guest token for tracking guest orders",
            example = "guest-12345-abcde"
    )
    private String guestToken;
    @Schema(
            description = "Order number for reference",
            example = "ORD-2024-001234",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String orderNumber;

    @Schema(
            description = "When the order was created",
            example = "2024-01-15T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "When the order was last updated",
            example = "2024-01-15T10:35:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime updatedAt;



    @AssertTrue(message = "Customer information must be provided")
    @JsonIgnore
    public boolean isUserInfoValid() {
        if (isGuest) {
            return guestName != null && !guestName.trim().isEmpty() &&
                    guestEmail != null && !guestEmail.trim().isEmpty();
        }

        // For registered users: check userName/email fields
        return userName != null && !userName.trim().isEmpty() &&
                email != null && !email.trim().isEmpty();
    }



    @JsonIgnore
    public String getEmailForService() {
        return email;
    }
    @JsonIgnore
    public String getNameForService() {
        if (userName != null && !userName.trim().isEmpty()) {
            return userName;
        }
        return userName;
    }


    @JsonIgnore
    public String getActualCustomerName() {
        if (userName != null && !userName.trim().isEmpty()) {
            return userName;
        }
        return guestName;
    }

    @JsonIgnore
    public String getActualCustomerEmail() {
        if (email != null) {
            return email;
        }
        return isGuest ? guestEmail : email;
    }

    public String getCustomerEmail() {
        return isGuest ? guestEmail : email;
    }

    public String getCustomerName() {
        return isGuest ? guestName : userName;
    }

    public int getTotalItemCount() {
        if (orderItems == null) return 0;
        return orderItems.stream()
                .mapToInt(OrderItemDTO::getQuantity)
                .sum();
    }

    public List<OrderItemDTO> getFoodItems() {
        if (orderItems == null) return Collections.emptyList();
        return orderItems.stream()
                .filter(item -> item.getItemType() == null ||
                        "FOOD".equalsIgnoreCase(item.getItemType()))
                .collect(Collectors.toList());
    }

    public String getEmail() {
        return email;
    }


    public List<OrderItemDTO> getDrinkItems() {
        if (orderItems == null) return Collections.emptyList();
        return orderItems.stream()
                .filter(item -> "DRINK".equalsIgnoreCase(item.getItemType()))
                .collect(Collectors.toList());
    }





}
