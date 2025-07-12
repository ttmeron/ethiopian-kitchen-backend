package com.resturant.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PaymentResponseDTO {

    private String clientSecret;
    private String paymentId;
    private String status;

}
