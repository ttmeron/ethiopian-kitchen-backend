package com.resturant.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Data
public class PaymentRequestDTO {
    private Long orderId;
    private String paymentMethod;
}
