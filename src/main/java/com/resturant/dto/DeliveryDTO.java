package com.resturant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryDTO {

    private Long id;
    private Long orderId;
    private String deliveryAddress;
    private LocalDateTime deliveryTime;
    private String status;
}
