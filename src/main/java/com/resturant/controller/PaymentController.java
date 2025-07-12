package com.resturant.controller;


import com.resturant.dto.PaymentRequestDTO;
import com.resturant.dto.PaymentResponseDTO;
import com.resturant.entity.PaymentStatus;
import com.resturant.mapper.PaymentMapper;
import com.resturant.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponseDTO> initiatePayment(@RequestBody PaymentRequestDTO paymentRequestDTO){
        PaymentResponseDTO response = paymentService.initiatePayment(paymentRequestDTO);
        return ResponseEntity.ok(response);
    }



    @PostMapping("/confirm")
    public ResponseEntity<String> manuallyConfirm(@RequestParam String paymentIntentId) {
        paymentService.updatePaymentStatus(paymentIntentId, PaymentStatus.SUCCESS);
        return ResponseEntity.ok("Payment marked as successful");
    }

}
