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

    private String paymentIntentId;

    public PaymentResponseDTO(String clientSecret, String paymentId, String status) {
        this.clientSecret = clientSecret;
        this.paymentId = paymentId;
        this.status = status;
    }

}
