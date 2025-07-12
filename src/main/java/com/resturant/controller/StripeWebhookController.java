package com.resturant.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resturant.entity.Order;
import com.resturant.entity.OrderStatus;
import com.resturant.entity.Payment;
import com.resturant.entity.PaymentStatus;
import com.resturant.repository.OrderRepository;
import com.resturant.repository.PaymentRepository;
import com.resturant.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/webhooks/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {


    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    @PostMapping
    @Transactional
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader("Stripe-Signature") String sigHeader) {

        System.out.println("Webhook received with payload: " + payload);
        System.out.println("Stripe-Signature header: " + sigHeader);

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            System.out.println("Signature verification failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        String eventType = event.getType();
        System.out.println("Received Stripe event type: " + eventType);

        if ("payment_intent.succeeded".equals(eventType)) {
            handlePaymentIntentSucceeded(event);
        } else if ("charge.succeeded".equals(eventType)) {
            handleChargeSucceededManually(payload);
        } else {
            System.out.println("Unhandled event type: " + eventType);
        }

        return ResponseEntity.ok("");
    }

private void handlePaymentIntentSucceeded(Event event) {
    Optional<StripeObject> optionalObject = event.getDataObjectDeserializer().getObject();
    if (optionalObject.isPresent()) {
        StripeObject stripeObject = optionalObject.get();
        if (stripeObject instanceof PaymentIntent) {
            PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
            System.out.println("PaymentIntent succeeded: " + paymentIntent.getId());

            // ADD THIS LOG
            Payment payment = paymentRepository.findByPaymentId(paymentIntent.getId())
                    .orElse(null);
            if (payment == null) {
                System.out.println("No payment found for PaymentIntent ID: " + paymentIntent.getId());
            } else {
                System.out.println("Payment found, updating status");
                paymentService.updatePaymentStatus(paymentIntent.getId(), PaymentStatus.SUCCESS);
            }

        } else {
            System.out.println("Unexpected object type for payment_intent.succeeded: " + stripeObject.getClass());
        }
    } else {
        System.out.println("Failed to deserialize payment_intent.succeeded event data");
    }
}

    private void handleChargeSucceededManually(String payload) {
        try {
            // Use simple Jackson to parse payload
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(payload);
            JsonNode dataNode = rootNode.path("data").path("object");
            String paymentIntentId = dataNode.path("payment_intent").asText();

            System.out.println("Charge succeeded (manual parsing)");
            System.out.println("PaymentIntent ID from charge: " + paymentIntentId);

            // Retrieve PaymentIntent to proceed
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            processPaymentSuccess(paymentIntent);

        } catch (Exception e) {
            System.out.println("Error parsing charge.succeeded payload: " + e.getMessage());
        }
    }

    private void processPaymentSuccess(PaymentIntent intent) {
        String paymentId = intent.getId();
        String orderIdStr = intent.getMetadata().get("orderId");

        System.out.println("Processing paymentIntent: " + paymentId);
        System.out.println("Order ID from metadata: " + orderIdStr);

        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found yet."));

        if (payment == null) {
            System.out.println("Payment not found for ID: " + paymentId + ". Maybe not saved yet.");
            throw new RuntimeException("Payment not found yet.");
        }

        payment.setStatus("SUCCESS");
        paymentRepository.save(payment);

        Long orderId = Long.valueOf(orderIdStr);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(OrderStatus.PROCESSING);
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order);
    }
}