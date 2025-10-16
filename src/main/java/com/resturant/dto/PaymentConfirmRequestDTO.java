package com.resturant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaymentConfirmRequestDTO {

    private Long orderId;
    private String paymentIntentId;
    private String email;
    private String userName;
    private GuestOrderDTO guestOrderDTO;
}
