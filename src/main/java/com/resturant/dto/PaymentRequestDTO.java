package com.resturant.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

@Data
public class PaymentRequestDTO {
    private Long orderId;
    private String paymentMethod;

    private String guestEmail;
    private String guestName;
    private Double amount;
    private String paymentIntentId;

}
