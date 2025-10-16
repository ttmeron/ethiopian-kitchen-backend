package com.resturant.service;

import com.resturant.dto.GuestOrderDTO;
import com.resturant.dto.OrderDTO;
import com.resturant.dto.OrderItemDTO;
import com.resturant.dto.OrderItemIngredientDTO;
import com.resturant.dto.response.GuestOrderResponseDTO;
import com.resturant.entity.*;
import com.resturant.mapper.OrderItemMapper;
import com.resturant.mapper.OrderMapper;
import com.resturant.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuestOrderServiceImpl implements GuestOrderService{


    private final GuestUserService guestUserService;
    @Autowired
    private final OrderRepository orderRepository;
    private final OrderItemIngredientRepository orderItemIngredientRepository;
    private final IngredientRepository ingredientRepository;
    @Autowired
    private final OrderMapper orderMapper;
    @Autowired
    private final FoodRepository foodRepository;
    @Autowired
    private final OrderItemRepository orderItemRepository;
    @Autowired
    private final PaymentRepository paymentRepository;


    @Transactional
    public OrderDTO createGuestOrder( GuestOrderDTO guestOrderDTO) {

//
//        if (!orderDTO.isGuest()) {
//            throw new IllegalArgumentException("Use placeOrder() for registered users");
//        }// 1. Create temporary guest user

        OrderDTO orderDTO = mapGuestOrderToOrderDTO(guestOrderDTO);

        // 2. Create Order entity
        Order order = new Order();

        order.setIsGuest(true);
        order.setGuestName(orderDTO.getUserName());
        order.setGuestEmail(orderDTO.getEmail());
        order.setUser(null);
        order.setStatus(OrderStatus.PROCESSING);
        order.setPaymentStatus(PaymentStatus.PENDING);// generate tracking token
        order.setTrackingToken(orderDTO.getTrackingToken());
        order.setSpecialInstructions(orderDTO.getSpecialInstructions());

        // Save order first to generate orderId
//        order = orderRepository.save(order);

        // 3. Map OrderItemDTO to OrderItem entities
        List<OrderItem> orderItems = orderDTO.getOrderItems()
                .stream()
                .map(dto -> {
                    OrderItem item = new OrderItem();
                    item.setOrder(order);
                    Food food = foodRepository.findById(dto.getFoodId())
                            .orElseThrow(()-> new RuntimeException("Food not found: " + dto.getFoodId()));
                    item.setFood(food);
                    item.setQuantity(dto.getQuantity());
                    item.setPrice(dto.getPrice());
                    return item;
                }).collect(Collectors.toList());

        order.setOrderItems(orderItems);
        orderRepository.save(order);

        return  orderMapper.toDTO(order);
    }


    public OrderDTO mapGuestOrderToOrderDTO(GuestOrderDTO guestOrderDTO) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setUserName(guestOrderDTO.getGuestName());
        orderDTO.setEmail(guestOrderDTO.getGuestEmail());
        orderDTO.setOrderItems(guestOrderDTO.getOrderItemDTOS());
        orderDTO.setSpecialInstructions(guestOrderDTO.getSpecialInstructions());
        orderDTO.setGuest(true);
        // You can set defaults for fields not provided by guest
        orderDTO.setStatus(OrderStatus.PROCESSING);
        orderDTO.setPaymentStatus(PaymentStatus.PENDING);
        orderDTO.setTrackingToken(UUID.randomUUID().toString());
        return orderDTO;
    }

    @Override
    public GuestOrderResponseDTO createGuestOrderAfterPayment(GuestOrderDTO guestOrderDTO, User guest, String paymentIntentId) {
        // 1. Create the Order
        Order order = new Order();
        order.setGuestName(guest.getUserName());
        order.setGuestEmail(guest.getEmail());
        order.setUser(guest);
        order.setIsGuest(true);
        order.setStatus(OrderStatus.PROCESSING);
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setSpecialInstructions(guestOrderDTO.getSpecialInstructions());
        order.setTotalPrice(BigDecimal.valueOf(guestOrderDTO.getTotalAmount()));
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setTrackingToken(UUID.randomUUID().toString());

        Order savedOrder = orderRepository.save(order);

        // 2. Save OrderItems
        for (OrderItemDTO itemDTO : guestOrderDTO.getOrderItemDTOS()) {
            OrderItem item = new OrderItem();
            item.setOrder(savedOrder);
            Food food = foodRepository.findById(itemDTO.getFoodId())
                    .orElseThrow(() -> new IllegalArgumentException("Food not found with ID: " + itemDTO.getFoodId()));
            item.setFood(food);
            item.setPrice(itemDTO.getPrice());
            item.setQuantity(itemDTO.getQuantity());
            orderItemRepository.save(item);
        }

        // 3. Link payment (optional: save Payment entity)
        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setPaymentId(paymentIntentId);
        payment.setAmount(guestOrderDTO.getTotalAmount());
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        // 4. Map to DTO and return
        return orderMapper.toGuestResponseDTO(savedOrder);
    }


}




