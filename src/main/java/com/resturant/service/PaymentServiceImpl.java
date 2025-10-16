package com.resturant.service;

import com.resturant.dto.GuestOrderDTO;
import com.resturant.dto.OrderDTO;
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
import java.time.LocalDateTime;
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
    @Autowired
    private final OrderService orderService;
    @Autowired
    private final GuestOrderService guestOrderService;


    @Value("${stripe.secret-key}")
    private String stripeSecretKey;


    @PostConstruct
    public void init(){
        Stripe.apiKey = stripeSecretKey;
    }


    @Override
    public PaymentResponseDTO initiatePayment(PaymentRequestDTO requestDTO) {

        if (requestDTO.getOrderId() == null) {
            throw new IllegalArgumentException("Order ID cannot be null in payment request");
        }


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


            payment.setStatus(PaymentStatus.PENDING);


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

        payment.setStatus(status);
        paymentRepository.save(payment);
    }

    @Override
    public PaymentResponseDTO createPaymentIntent(PaymentRequestDTO request) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", (long) (request.getAmount() * 100)); // convert to cents
        params.put("currency", "usd");

        Map<String, Object> autoPaymentMethods = new HashMap<>();
        autoPaymentMethods.put("enabled", true);
        params.put("automatic_payment_methods", autoPaymentMethods);

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        return new PaymentResponseDTO(
                paymentIntent.getClientSecret(),
                paymentIntent.getId(),
                paymentIntent.getStatus()
        );
    }

    @Override
    public PaymentResponseDTO confirmPayment(String paymentIntentId) {
        return null;
    }
//
//    @Override
//    public PaymentResponseDTO confirmGuestPayment(String paymentIntentId, GuestOrderDTO guestOrderDTO) {
//        try {
//            // 1. Retrieve PaymentIntent from Stripe
//            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
//            PaymentIntent confirmedIntent = intent.confirm();
//
//            // 2. Find local Payment
//            Payment payment = paymentRepository.findByPaymentId(paymentIntentId)
//                    .orElseThrow(() -> new RuntimeException("Payment not found"));
//
//            if ("succeeded".equals(confirmedIntent.getStatus())) {
//                payment.setStatus(PaymentStatus.SUCCESS);
//                paymentRepository.save(payment);
//
//                // 3. Create guest order AFTER successful payment
//                // If you don’t have a guest `User` entity, you can pass null here
//                guestOrderService.createGuestOrderAfterPayment(
//                        guestOrderDTO,
//                        null,  // optional guest user entity if you want
//                        confirmedIntent.getId()
//                );
//            } else {
//                payment.setStatus(PaymentStatus.FAILED);
//                paymentRepository.save(payment);
//            }
//
//            // 4. Return response
//            return new PaymentResponseDTO(
//                    confirmedIntent.getClientSecret(),
//                    confirmedIntent.getId(),
//                    confirmedIntent.getStatus()
//            );
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to confirm guest payment: " + e.getMessage(), e);
//        }
//    }

    @Override
    public void confirmGuestPayment(String paymentIntentId, GuestOrderDTO guestOrderDTO) {
        // 1. Find the payment (should exist now)
        Payment payment = paymentRepository.findByPaymentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found for intentId: " + paymentIntentId));

        // 2. Update payment status to SUCCESS
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        // 3. Create guest order and link it to payment
        if (payment.getOrder() == null) {
            OrderDTO orderDTO = orderService.createGuestOrderAfterPayment(guestOrderDTO, paymentIntentId);

            // The order should now be linked to payment via createGuestOrderAfterPayment
            System.out.println("Guest order created with ID: " + orderDTO.getOrderId());
        } else {
            // Update existing order status
            Order order = payment.getOrder();
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        }
    }





    @Override
    public void handlePaymentSuccess(String paymentIntentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);
    }

}
