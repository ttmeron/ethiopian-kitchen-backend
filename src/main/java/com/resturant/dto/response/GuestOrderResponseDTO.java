package com.resturant.dto.response;

import com.resturant.dto.DeliveryDTO;
import com.resturant.dto.OrderDTO;
import com.resturant.dto.OrderItemDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GuestOrderResponseDTO {
    private Long orderId;
    private String placedTime;
    private BigDecimal totalPrice;
    private String trackingToken;
    private String status; // or OrderStatus
    private String paymentStatus;
    private List<OrderItemDTO> orderItems;
    private DeliveryDTO deliveryDTO;
    private boolean isGuest;
}
