package com.resturant.dto;

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

public class OrderDTO {

    private Long orderId;
    private BigDecimal totalPrice;
    private String status;
    @NotNull
    private String userName;
    @Email
    private String email;


    @Valid
    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemDTO> orderItems;
    private DeliveryDTO deliveryDTO;
}
