package com.resturant.service;

import com.resturant.dto.GuestOrderDTO;
import com.resturant.dto.OrderDTO;
import com.resturant.dto.PaymentRequestDTO;
import com.resturant.dto.PaymentResponseDTO;
import com.stripe.exception.StripeException;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    OrderDTO placeOrder(OrderDTO orderDTO);
    OrderDTO getOrderById(Long id);
    List<OrderDTO> findByUserEmail(String email);
    List<OrderDTO> getAllOrder();
    OrderDTO updateOrder(Long id, OrderDTO orderDTO);
    void deleteOrder(Long id);
    OrderDTO markAsReady(Long id);
    List<OrderDTO> getPaidOrders();
    void attachTrackingToken(Long orderId, String trackingToken);
    OrderDTO getOrderByTrackingToken(String trackingToken);
    OrderDTO placeGuestOrder(GuestOrderDTO guestOrderDTO);
    OrderDTO payForOrder(Long orderId, PaymentRequestDTO paymentRequestDTO);
    PaymentResponseDTO createGuestPaymentIntent(GuestOrderDTO guestOrderDTO) throws StripeException;
    public OrderDTO createGuestOrderAfterPayment(GuestOrderDTO guestOrderDTO, String paymentIntentId);




}
