package com.resturant.controller;

import com.resturant.dto.*;
import com.resturant.entity.Order;
import com.resturant.entity.PaymentStatus;
import com.resturant.entity.User;
import com.resturant.mapper.PaymentMapper;
import com.resturant.repository.UserRepository;
import com.resturant.service.GuestUserService;
import com.resturant.service.OrderService;
import com.resturant.service.PaymentService;
import com.resturant.service.UserService;
import com.stripe.model.PaymentIntent;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final GuestUserService guestUserService;
    private final OrderService orderService;



    @PostMapping("/initiate")
    @Operation(summary = "Initiate payment for a guest order")
    public ResponseEntity<PaymentResponseDTO> initiateGuestPayment(@RequestBody @Valid GuestOrderDTO guestOrderDTO) {


        System.out.println("Received guest order: " + guestOrderDTO);
        try {
            // Use GuestOrderDTO directly
            PaymentResponseDTO paymentResponse = orderService.createGuestPaymentIntent(guestOrderDTO);
            return ResponseEntity.ok(paymentResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @PostMapping("/confirm")
    public ResponseEntity<Map<String, String>> manuallyConfirm(@RequestBody PaymentConfirmRequestDTO request) {
        if(request.getGuestOrderDTO() != null) {
            paymentService.confirmGuestPayment(request.getPaymentIntentId(), request.getGuestOrderDTO());
        }else {

            paymentService.confirmPayment(request.getPaymentIntentId());
        }
        Map<String, String> response = new HashMap<>();
        response.put("message", "Payment confirmed successfully");
        response.put("paymentIntentId", request.getPaymentIntentId());

        return ResponseEntity.ok(response);
    }


}
