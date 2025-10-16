package com.resturant.service;

import com.resturant.dto.GuestOrderDTO;
import com.resturant.dto.PaymentRequestDTO;
import com.resturant.dto.PaymentResponseDTO;
import com.resturant.entity.PaymentStatus;
import com.stripe.exception.StripeException;

public interface PaymentService {

    PaymentResponseDTO initiatePayment(PaymentRequestDTO requestDTO);
    void handlePaymentSuccess(String paymentIntentId);
    void updatePaymentStatus(String paymentIntentId, PaymentStatus newStatus);
    PaymentResponseDTO createPaymentIntent(PaymentRequestDTO request) throws StripeException;
    PaymentResponseDTO confirmPayment(String paymentIntentId);

    void confirmGuestPayment(String paymentIntentId, GuestOrderDTO guestOrderDTO);





}
