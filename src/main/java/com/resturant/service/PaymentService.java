package com.resturant.service;

import com.resturant.dto.PaymentRequestDTO;
import com.resturant.dto.PaymentResponseDTO;
import com.resturant.entity.PaymentStatus;

public interface PaymentService {

    PaymentResponseDTO initiatePayment(PaymentRequestDTO requestDTO);
    void handlePaymentSuccess(String paymentIntentId);
    public void updatePaymentStatus(String paymentIntentId, PaymentStatus newStatus);
}
