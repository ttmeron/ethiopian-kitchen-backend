package com.resturant.service;

import com.resturant.dto.GuestOrderDTO;
import com.resturant.dto.OrderDTO;
import com.resturant.dto.OrderItemDTO;
import com.resturant.dto.response.GuestOrderResponseDTO;
import com.resturant.entity.*;
import com.resturant.mapper.OrderMapper;
import com.resturant.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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


        OrderDTO orderDTO = mapGuestOrderToOrderDTO(guestOrderDTO);

        Order order = new Order();

        order.setIsGuest(true);
        order.setGuestName(orderDTO.getUserName());
        order.setGuestEmail(orderDTO.getEmail());
        order.setUser(null);
        order.setStatus(OrderStatus.PROCESSING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setTrackingToken(orderDTO.getTrackingToken());
        order.setSpecialInstructions(orderDTO.getSpecialInstructions());

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemDTO dto : orderDTO.getOrderItems()) {
            OrderItem item = new OrderItem();
            item.setOrder(order);

            if (dto.getItemType() != null && "DRINK".equalsIgnoreCase(dto.getItemType())) {

                item.setSize(dto.getSize());
                item.setIceOption(dto.getIceOption());
            }
                    Food food = foodRepository.findById(dto.getFoodId())
                            .orElseThrow(()-> new RuntimeException("Food not found: " + dto.getFoodId()));
                    item.setFood(food);
                    item.setQuantity(dto.getQuantity());
                    item.setPrice(dto.getPrice());
            orderItems.add(item);
        }


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
        orderDTO.setStatus(OrderStatus.PROCESSING);
        orderDTO.setPaymentStatus(PaymentStatus.PENDING);
        orderDTO.setTrackingToken(UUID.randomUUID().toString());
        return orderDTO;
    }

    @Override
    public GuestOrderResponseDTO createGuestOrderAfterPayment(GuestOrderDTO guestOrderDTO, User guest, String paymentIntentId) {

        Order order = new Order();

        order.setGuestName(guest.getUserName());
        order.setGuestEmail(guest.getEmail());
        order.setUser(guest);
        order.setIsGuest(true);
        order.setStatus(OrderStatus.PROCESSING);
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setSpecialInstructions(guestOrderDTO.getSpecialInstructions());
        order.setTotalPrice(guestOrderDTO.getTotalAmount());
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setTrackingToken(UUID.randomUUID().toString());

        Order savedOrder = orderRepository.save(order);

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

        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setPaymentId(paymentIntentId);
        payment.setAmount(guestOrderDTO.getTotalAmount());
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        return orderMapper.toGuestResponseDTO(savedOrder);
    }


}




