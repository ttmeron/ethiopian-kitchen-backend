package com.resturant.dto;

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
            description = "Total price of the order in Ethiopian Birr",
            example = "750.50",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private BigDecimal totalPrice;
    @Schema(
            description = "Current status of the order",
            example = "PROCESSING",
            allowableValues = {"NEW", "PROCESSING", "DELIVERED", "CANCELLED"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String status;
    @Schema(
            description = "Name of the customer placing the order",
            example = "Abebe Kebede",
            requiredMode = Schema.RequiredMode.REQUIRED
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
                    requiredMode = Schema.RequiredMode.REQUIRED
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
}
