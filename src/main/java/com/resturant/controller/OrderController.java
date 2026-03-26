package com.resturant.controller;


import com.resturant.dto.*;
import com.resturant.entity.Order;
import com.resturant.entity.OrderStatus;
import com.resturant.mapper.OrderMapper;
import com.resturant.repository.OrderRepository;
import com.resturant.service.GuestOrderService;
import com.resturant.service.GuestUserService;
import com.resturant.service.OrderService;
import com.stripe.model.PaymentIntent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
@Slf4j
@Tag(name = "Order", description = "Ethiopian Kitchen Order API")
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    GuestUserService guestUserService;

    @Autowired
    OrderMapper orderMapper;
    @Autowired
    GuestOrderService guestOrderService;

    @PostMapping
    @Operation(summary = "Add a new order item")
    public ResponseEntity<?> placeOrder(@Valid @RequestBody OrderDTO orderDTO,  BindingResult bindingResult, HttpServletRequest request){

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        if (orderDTO.isGuest()) {
            throw new IllegalArgumentException("Use /orders/guest for guest users");
        }
        OrderDTO order = orderService.placeOrder(orderDTO);
        return ResponseEntity.ok(order);
    }



    @PostMapping("/guest")
    @Operation(summary = "Place order as guest")
    public ResponseEntity<?> placeGuestOrder(
            @Valid @RequestBody GuestOrderDTO guestOrderDTO,
            BindingResult bindingResult,
            HttpServletRequest request) {


        for (int i = 0; i < guestOrderDTO.getOrderItemDTOS().size(); i++) {
            OrderItemDTO item = guestOrderDTO.getOrderItemDTOS().get(i);

        }
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }


        OrderDTO order = orderService.placeGuestOrder(guestOrderDTO);

        return ResponseEntity.ok(order);

    }

    @PutMapping("/{id}/pay")
    @Operation(summary = "Pay for an order")
    public ResponseEntity<?> payForOrder(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequestDTO paymentRequest) {
        try {
            System.out.println("=== PAYMENT DEBUG ===");
            System.out.println("Order ID: " + id);
            System.out.println("Payment Request: " + paymentRequest);

            OrderDTO dto = orderService.payForOrder(id, paymentRequest);
            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            System.err.println("=== PAYMENT ERROR ===");
            e.printStackTrace();
            // Return the actual error message
            return ResponseEntity.status(500).body("Payment failed: " + e.getMessage());
        }
    }


    @GetMapping("/track/{trackingToken}")
    public ResponseEntity<OrderDTO> getOrderByTrackingToken(@PathVariable String trackingToken) {
        OrderDTO order = orderService.getOrderByTrackingToken(trackingToken);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a order  by ID")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id){
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/by-email")
    @Operation(summary = "Get a order  by email")
    public ResponseEntity<List<OrderDTO>> getOrderByEmail(@RequestParam String email){
        List<OrderDTO> orders = orderService.findByUserEmail(email);
        return ResponseEntity.ok(orders);
    }

    @GetMapping
    @Operation(summary = "Get all order items",
            description = "Returns a list of all Ethiopian Kitchen orders")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved order")
    public ResponseEntity<List<OrderDTO>> getAllOrder(){
        return ResponseEntity.ok(orderService.getAllOrder());
    }

    @PutMapping("/{id}")
    @Operation(summary = "update order")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Long id, @RequestBody OrderDTO orderDTO){
        return ResponseEntity.ok(orderService.updateOrder(id,orderDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "delete unwanted order")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id){
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{orderId}/ready")
    public ResponseEntity<OrderDTO> markOrderAsReady(@PathVariable Long orderId) {
        OrderDTO updatedOrder = orderService.markAsReady(orderId);
        return ResponseEntity.ok(updatedOrder);
    }
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(@PathVariable OrderStatus status) {


        List<Order> orders = orderRepository.findByStatus(status);
        List<OrderDTO> orderDTOs = orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
    }

    @GetMapping("/paid")
    public List<OrderDTO> getPaidOrders() {
        return orderService.getPaidOrders();
    }

    @PostMapping("/initiate")
    public PaymentResponseDTO initiatePayment(@RequestBody PaymentRequestDTO paymentRequestDTO) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("amount", (long) (paymentRequestDTO.getAmount() * 100)); // Stripe wants cents
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
        } catch (Exception e) {
            throw new RuntimeException("Failed to create payment intent", e);
        }
    }






}
