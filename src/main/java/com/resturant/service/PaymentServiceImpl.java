package com.resturant.service;

import com.resturant.dto.PaymentRequestDTO;
import com.resturant.dto.PaymentResponseDTO;
import com.resturant.entity.Order;
import com.resturant.entity.OrderStatus;
import com.resturant.entity.Payment;
import com.resturant.entity.PaymentStatus;
import com.resturant.mapper.PaymentMapper;
import com.resturant.repository.OrderRepository;
import com.resturant.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentRepository paymentRepository;


    @Value("${stripe.secret-key}")
    private String stripeSecretKey;


    @PostConstruct
    public void init(){
        Stripe.apiKey = stripeSecretKey;
    }


    @Override
    public PaymentResponseDTO initiatePayment(PaymentRequestDTO requestDTO) {

        Order order = orderRepository.findById(requestDTO.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("amount", order.getTotalPrice().multiply(BigDecimal.valueOf(100)).intValue());
            params.put("currency", "usd");
            params.put("payment_method_types", Arrays.asList("card"));
            params.put("metadata", Collections.singletonMap("orderId", order.getId()));

            PaymentIntent intent = PaymentIntent.create(params);

            System.out.println("PaymentIntent ID: " + intent.getId());
            System.out.println("PaymentIntent clientSecret: " + intent.getClientSecret());
            String clientSecret = intent.getClientSecret();
            Payment payment = paymentMapper.toEntity(requestDTO, intent.getId(), order.getTotalPrice());

            payment.setOrder(order);


            payment.setStatus(PaymentStatus.PENDING.name());


            paymentRepository.save(payment);
            PaymentResponseDTO responseDto = paymentMapper.toDto(payment, clientSecret);
            System.out.println("Response DTO: " + responseDto);
            return responseDto;
        }catch (StripeException e){
            throw new RuntimeException("Stripe payment failed: " + e.getMessage());
        }
    }


    public void updatePaymentStatus(String paymentIntentId, PaymentStatus status) {
        Payment payment = paymentRepository.findByPaymentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(status.name());
        paymentRepository.save(payment);
    }


    @Override
    public void handlePaymentSuccess(String paymentIntentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus("SUCCESS");
        paymentRepository.save(payment);
    }

}
