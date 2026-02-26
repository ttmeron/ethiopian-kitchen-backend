package com.resturant.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.resturant.dto.*;
import com.resturant.entity.*;
import com.resturant.exception.ResourceNotFoundException;
import com.resturant.mapper.DeliveryMapper;
import com.resturant.mapper.OrderItemIngredientMapper;
import com.resturant.mapper.OrderItemMapper;
import com.resturant.mapper.OrderMapper;
import com.resturant.repository.*;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderRepository orderRepository;
    @Autowired
    PaymentService paymentService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    FoodRepository foodRepository;
    @Autowired
    DeliveryRepository deliveryRepository;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    OrderItemRepository orderItemRepository;
    @Autowired
    OrderItemIngredientRepository orderItemIngredientRepository;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    DeliveryMapper deliveryMapper;
    @Autowired
    OrderItemMapper orderItemMapper;
    @Autowired
    SoftDrinkRepository softDrinkRepository;
    @Autowired
    OrderItemIngredientMapper orderItemIngredientMapper;
    private final ObjectMapper objectMapper;


    @Transactional
    @Override
    public OrderDTO placeOrder(OrderDTO orderDTO) {
        if (orderDTO == null)
            throw new IllegalArgumentException("OrderDTO cannot be null");

        if (orderDTO.isGuest())
            throw new IllegalArgumentException("Use placeGuestOrder for guest users");

        User user = userRepository.findByEmail(orderDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found: " + orderDTO.getEmail()));

        Order order = new Order();
        order.setUser(user);
        order.setIsGuest(false);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setSpecialInstructions(orderDTO.getSpecialInstructions());
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setGuestToken(generateGuestToken());
        order.setTrackingToken(generateTrackingToken());
        order.setCreatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        List<OrderItem> savedItems = new ArrayList<>();
        BigDecimal orderSubtotal = BigDecimal.ZERO;


        for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {

            if (itemDTO.getItemType() != null &&
                  "DRINK".equalsIgnoreCase(itemDTO.getItemType())) {
                SoftDrink drink = softDrinkRepository.findActiveById(itemDTO.getDrinkId())
                        .orElseThrow(() -> new RuntimeException("Drink not found: " + itemDTO.getDrinkId()));

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setDrink(drink);
                orderItem.setItemType(OrderItem.ItemType.DRINK);
                orderItem.setQuantity(itemDTO.getQuantity());
                if (itemDTO.getSize() != null) {
                    orderItem.setSize(itemDTO.getSize());
                }
                if (itemDTO.getIceOption() != null) {
                    orderItem.setIceOption(itemDTO.getIceOption());
                }

                BigDecimal basePrice = drink.getPrice();

                if (itemDTO.getSize() != null) {

                    BigDecimal multiplier = getSizeMultiplier(itemDTO.getSize());
                    basePrice = basePrice.multiply(multiplier);
                }

                BigDecimal backendItemPrice = basePrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

                orderItem.setPrice(backendItemPrice);

                Map<String, String> customizations = new HashMap<>();
                if (itemDTO.getSize() != null) customizations.put("size", itemDTO.getSize());
                if (itemDTO.getIceOption() != null) customizations.put("ice", itemDTO.getIceOption());

                try {
                    java.lang.reflect.Method setCustomizationsMethod = OrderItem.class.getMethod("setCustomizations", String.class);

                    setOrderItemCustomizations(orderItem, customizations);
                }catch (NoSuchMethodException e) {
                    log.warn("OrderItem entity doesn't have setCustomizations method. Please add this field to the entity.");
                }

                OrderItem savedOrderItem = orderItemRepository.save(orderItem);
                savedItems.add(savedOrderItem);

                orderSubtotal = orderSubtotal.add(backendItemPrice);


                continue;
            }

            Food food = foodRepository.findById(itemDTO.getFoodId())
                    .orElseThrow(() -> new RuntimeException("Food not found: " + itemDTO.getFoodId()));
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setFood(food);
            orderItem.setItemType(OrderItem.ItemType.FOOD);
            orderItem.setQuantity(itemDTO.getQuantity());

            BigDecimal basePrice = food.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            BigDecimal ingredientsTotal = BigDecimal.ZERO;


            List<OrderItemIngredient> itemIngredients = new ArrayList<>();
            if (itemDTO.getCustomIngredients() != null) {
                for (OrderItemIngredientDTO ingDTO : itemDTO.getCustomIngredients()) {
                    if (ingDTO.getIngredientId() == null) {
                        throw new IllegalArgumentException("Ingredient ID cannot be null");
                    }
                    Ingredient ingredient = ingredientRepository.findById(ingDTO.getIngredientId())
                            .orElseThrow(() -> new RuntimeException("Ingredient not found: " + ingDTO.getIngredientId()));

                    OrderItemIngredient oi = new OrderItemIngredient();
                    oi.setOrderItem(orderItem);
                    oi.setIngredient(ingredient);
                    oi.setQuantity(ingDTO.getQuantity() != 0 ? ingDTO.getQuantity() : 1);
                    oi.setExtraCost(ingredient.getExtraCost());

                    itemIngredients.add(oi);

                    BigDecimal ingredientCost = ingredient.getExtraCost().multiply(BigDecimal.valueOf(oi.getQuantity()));
                    ingredientsTotal = ingredientsTotal.add(ingredientCost);

                }
            }

            BigDecimal backendItemPrice = basePrice.add(ingredientsTotal);
            orderItem.setPrice(backendItemPrice);

            OrderItem savedOrderItem = orderItemRepository.save(orderItem);

            if (!itemIngredients.isEmpty()) {
                itemIngredients.forEach(ing -> ing.setOrderItem(savedOrderItem));
                orderItemIngredientRepository.saveAll(itemIngredients);
                savedOrderItem.setOrderItemIngredients(itemIngredients);
            }

            savedItems.add(savedOrderItem);

            orderSubtotal = orderSubtotal.add(backendItemPrice);
        }

        BigDecimal taxRate = new BigDecimal("0.08");
        BigDecimal taxAmount = orderSubtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal backendTotalPrice = orderSubtotal.add(taxAmount);

        if (orderDTO.getTotalPrice() != null) {
            BigDecimal frontendTotalPrice = orderDTO.getTotalPrice();
            BigDecimal difference = frontendTotalPrice.subtract(backendTotalPrice).abs();
            BigDecimal tolerance = new BigDecimal("0.01");


            if (difference.compareTo(tolerance) > 0) {
                System.out.println("⚠️  WARNING: Price discrepancy detected!");

            }
            System.out.println("=== END VALIDATION ===");
        }

        order.setTotalPrice(backendTotalPrice);
        order = orderRepository.save(order);

        return orderMapper.toDTO(order);
    }

    @Transactional
    @Override
    public OrderDTO placeGuestOrder(GuestOrderDTO guestOrderDTO) {

        if (guestOrderDTO == null) throw new IllegalArgumentException("Guest order cannot be null");

        Order order = new Order();
        order.setIsGuest(true);
        order.setGuestName(guestOrderDTO.getGuestName());
        order.setGuestEmail(guestOrderDTO.getGuestEmail());
        order.setGuestToken(guestOrderDTO.getGuestToken());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setSpecialInstructions(guestOrderDTO.getSpecialInstructions());
        order.setOrderNumber(generateOrderNumber());
        order.setTrackingToken(generateTrackingToken());
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal orderTotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemDTO itemDTO : guestOrderDTO.getOrderItemDTOS()) {

            if ("DRINK".equalsIgnoreCase(itemDTO.getItemType())) {

                SoftDrink drink = softDrinkRepository
                        .findActiveById(itemDTO.getDrinkId())
                        .orElseThrow(() -> new RuntimeException("Drink not found"));

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setDrink(drink);
                orderItem.setItemType(OrderItem.ItemType.DRINK);
                orderItem.setQuantity(itemDTO.getQuantity());
                orderItem.setSize(itemDTO.getSize());
                orderItem.setIceOption(itemDTO.getIceOption());

                BigDecimal basePrice = drink.getPrice();

                if (itemDTO.getSize() != null) {
                    basePrice = basePrice.multiply(getSizeMultiplier(itemDTO.getSize()));
                }
                BigDecimal itemPrice = basePrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
                orderItem.setPrice(itemPrice);

                Map<String, String> customizations = new HashMap<>();
                if (itemDTO.getSize() != null) customizations.put("size", itemDTO.getSize());
                if (itemDTO.getIceOption() != null) customizations.put("ice", itemDTO.getIceOption());

                setOrderItemCustomizations(orderItem, customizations);

                orderItems.add(orderItem);
                orderTotal = orderTotal.add(itemPrice);

                continue;
            }

            Food food = foodRepository.findById(itemDTO.getFoodId())
                    .orElseThrow(() -> new RuntimeException("Food not found: " + itemDTO.getFoodId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setFood(food);
            orderItem.setItemType(OrderItem.ItemType.FOOD);
            orderItem.setQuantity(itemDTO.getQuantity());

            List<OrderItemIngredient> ingredients = new ArrayList<>();
            BigDecimal extraCost = BigDecimal.ZERO;

            if (itemDTO.getCustomIngredients() != null) {
                for (OrderItemIngredientDTO ciDTO : itemDTO.getCustomIngredients()) {

                    Ingredient ingredient = ingredientRepository.findById(ciDTO.getIngredientId())
                            .orElseThrow(() -> new RuntimeException("Ingredient not found: " + ciDTO.getIngredientId()));

                    OrderItemIngredient oi = new OrderItemIngredient();
                    oi.setOrderItem(orderItem);
                    oi.setIngredient(ingredient);
                    oi.setExtraCost(ingredient.getExtraCost() != null ? ingredient.getExtraCost() : BigDecimal.ZERO);

                    ingredients.add(oi);
                    extraCost = extraCost.add(oi.getExtraCost());
                }
            }

            orderItem.setOrderItemIngredients(ingredients);

            BigDecimal totalPrice = (food.getPrice().add(extraCost)).multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

            orderItem.setPrice(totalPrice);

            orderItems.add(orderItem);
            orderTotal = orderTotal.add(totalPrice);
        }
        System.out.println("Set " + orderItems.size() + " items to order");

        order.setOrderItems(orderItems);

        BigDecimal taxRate = new BigDecimal("0.08");
        BigDecimal taxAmount = orderTotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalWithTax = orderTotal.add(taxAmount);
        order.setTotalPrice(totalWithTax);

        Order savedOrder = orderRepository.save(order);

        return orderMapper.toDTO(savedOrder);
    }


    @Transactional
    @Override
    public OrderDTO payForOrder(Long orderId, PaymentRequestDTO paymentRequest) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getTotalPrice() == null) {
            throw new RuntimeException("Order total price is not calculated yet!");
        }

        PaymentRequestDTO stripeRequest = new PaymentRequestDTO();
        stripeRequest.setOrderId(orderId);
        stripeRequest.setAmount(order.getTotalPrice().doubleValue());
        stripeRequest.setPaymentMethod(paymentRequest.getPaymentMethod());
        stripeRequest.setGuestEmail(paymentRequest.getGuestEmail());
        stripeRequest.setGuestName(paymentRequest.getGuestName());

        PaymentResponseDTO paymentResponse = paymentService.initiatePayment(stripeRequest);

        paymentService.confirmPayment(paymentResponse.getPaymentIntentId());

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        return orderMapper.toDTO(order);
    }

    @Override
    public PaymentResponseDTO createGuestPaymentIntent(GuestOrderDTO guestOrderDTO) throws StripeException {
        BigDecimal amountInCents = (BigDecimal) (guestOrderDTO.getTotalAmount() .multiply(BigDecimal.valueOf(100)));

        try{
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amountInCents);
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
            e.printStackTrace();
        throw new RuntimeException("Error creating payment intent", e);
    }
    }
    private BigDecimal getSizeMultiplier(String size) {
        if (size == null) return BigDecimal.ONE;

        String sizeLower = size.toLowerCase();
        switch(sizeLower) {
            case "small":
            case "250ml":
                return BigDecimal.ONE;
            case "medium":
            case "500ml":
            case "regular":
                return new BigDecimal("1.5");
            case "large":
            case "1l":
                return new BigDecimal("2.0");
            default:
                return BigDecimal.ONE;
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    private String generateGuestToken() {
        return UUID.randomUUID().toString();
    }

    private String generateTrackingToken() {
        return "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    @Override
    public OrderDTO createGuestOrderAfterPayment(GuestOrderDTO guestOrderDTO, String paymentIntentId) {

        Optional<Order> existingOrderOpt = orderRepository.findFirstByGuestTokenAndStatusOrderByCreatedAtDesc(guestOrderDTO.getGuestToken(), OrderStatus.PENDING);
        Order guestOrder;

        if (existingOrderOpt.isPresent()) {
            guestOrder = existingOrderOpt.get();
            guestOrder.setStatus(OrderStatus.CONFIRMED);
            guestOrder.setPaymentStatus(PaymentStatus.PAID);
            guestOrder.setUpdatedAt(LocalDateTime.now());
            guestOrder.setSpecialInstructions(guestOrderDTO.getSpecialInstructions());

            updateOrderItems(guestOrder, guestOrderDTO.getOrderItemDTOS());

            recalculateOrderTotal(guestOrder);

        } else {
            guestOrder = new Order();
            guestOrder.setIsGuest(true);
            guestOrder.setGuestEmail(guestOrderDTO.getGuestEmail());
            guestOrder.setGuestName(guestOrderDTO.getGuestName());
            guestOrder.setGuestToken(guestOrderDTO.getGuestToken());
            guestOrder.setSpecialInstructions(guestOrderDTO.getSpecialInstructions());
            guestOrder.setStatus(OrderStatus.CONFIRMED);
            guestOrder.setPaymentStatus(PaymentStatus.PAID);
            guestOrder.setOrderNumber("TEMP-" + UUID.randomUUID());
            guestOrder.setCreatedAt(LocalDateTime.now());

            updateOrderItems(guestOrder, guestOrderDTO.getOrderItemDTOS());
        }

        guestOrder = orderRepository.save(guestOrder);

        Payment payment = paymentRepository.findByPaymentId(paymentIntentId)
                .orElse(new Payment());
        payment.setOrder(guestOrder);
        payment.setPaymentId(paymentIntentId);
        payment.setAmount(guestOrder.getTotalPrice());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        return orderMapper.toDTO(guestOrder);
    }

    @Override
    public List<OrderDTO> getOrdersByUserEmailAndStatus(String email, OrderStatus status) {
        return null;
    }

    @Override
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        return null;
    }


    @Override
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Order not found with Order Id: " + id));

        return orderMapper.toDTO(order);
    }

    @Override
    public List<OrderDTO> findByUserEmail(String email) {
        List<Order> orders = orderRepository.findByUserEmailOrGuestEmail(email,email);
        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Override
    public List<OrderDTO> getAllOrder() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public OrderDTO updateOrder(Long orderId, OrderDTO orderDTO) {


        Order existingOrder = orderRepository.findByIdWithRelations(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));


        existingOrder.setSpecialInstructions(orderDTO.getSpecialInstructions());

        updateOrderItems(existingOrder, orderDTO.getOrderItems());

        updateOrderDelivery(existingOrder, orderDTO.getDeliveryDTO());

        recalculateOrderTotal(existingOrder);

        Order updatedOrder = orderRepository.save(existingOrder);
        return orderMapper.toDTO(updatedOrder);

    }

    @Override
    public void deleteOrder(Long id) {

        orderRepository.deleteById(id);

    }

    @Override
    public OrderDTO markAsReady(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        order.setStatus(OrderStatus.READY);
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);

        return orderMapper.toDTO(order);

    }

    @Override
    public List<OrderDTO> getPaidOrders() {
        List<Order> orders = orderRepository.findByPaymentStatusAndStatus(PaymentStatus.SUCCESS, OrderStatus.PROCESSING);
        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void attachTrackingToken(Long orderId, String trackingToken) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setTrackingToken(trackingToken);
        orderRepository.save(order);
    }

    @Override
    public OrderDTO getOrderByTrackingToken(String trackingToken) {
        Order order = orderRepository.findByTrackingToken(trackingToken)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with tracking token: " + trackingToken));

        return orderMapper.toDTO(order);
    }


private BigDecimal calculateOrderTotal(OrderDTO orderDTO) {
    return orderDTO.getOrderItems().stream()
            .map(itemDTO -> {

                if ("DRINK".equalsIgnoreCase(itemDTO.getItemType())) {
                    SoftDrink drink = softDrinkRepository.findById(itemDTO.getDrinkId())
                            .orElseThrow(() -> new ResourceNotFoundException("Drink not found"));

                    BigDecimal price = drink.getPrice();
                    if (itemDTO.getSize() != null) {
                        price = price.multiply(getSizeMultiplier(itemDTO.getSize()));
                    }

                    return price.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
                }

                Food food = foodRepository.findById(itemDTO.getFoodId())
                        .orElseThrow(() -> new ResourceNotFoundException("Food not found"));

                BigDecimal base = food.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
                BigDecimal extras = BigDecimal.ZERO;

                if (itemDTO.getCustomIngredients() != null) {
                    for (OrderItemIngredientDTO ing : itemDTO.getCustomIngredients()) {
                        Ingredient ingredient = ingredientRepository.findById(ing.getIngredientId())
                                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));
                        extras = extras.add(
                                ingredient.getExtraCost().multiply(BigDecimal.valueOf(ing.getQuantity()))
                        );
                    }
                }
                return base.add(extras);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
}


    private List<OrderItem> processOrderItems(Order order, OrderDTO orderDTO) {
        return orderDTO.getOrderItems().stream()
                .map(itemDTO -> {
                    Food food = foodRepository.findById(itemDTO.getFoodId())
                            .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + itemDTO.getFoodId()));

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setFood(food);
                    orderItem.setQuantity(itemDTO.getQuantity());

                    BigDecimal basePrice = food.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

                    if (itemDTO.getCustomIngredients() != null && !itemDTO.getCustomIngredients().isEmpty()) {
                        List<OrderItemIngredient> ingredients = itemDTO.getCustomIngredients().stream()
                                .map(ingDTO -> {
                                    Ingredient ing = ingredientRepository.findById(ingDTO.getIngredientId())
                                            .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with ID: " + ingDTO.getIngredientId()));

                                    OrderItemIngredient orderItemIngredient = new OrderItemIngredient();
                                    orderItemIngredient.setOrderItem(orderItem);
                                    orderItemIngredient.setIngredient(ing);
                                    orderItemIngredient.setQuantity(ingDTO.getQuantity());
                                    orderItemIngredient.setExtraCost(ing.getExtraCost());

                                    return orderItemIngredient;
                                })
                                .collect(Collectors.toList());

                        BigDecimal ingredientsTotal = ingredients.stream()
                                .map(ing -> ing.getExtraCost().multiply(BigDecimal.valueOf(ing.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        basePrice = basePrice.add(ingredientsTotal);
                        orderItem.setOrderItemIngredients(ingredients);
                    }

                    orderItem.setPrice(basePrice);

                    return orderItem;
                })
                .collect(Collectors.toList());
    }



    private BigDecimal calculateOrderItemTotal(OrderItemDTO itemDTO, Food food) {
        BigDecimal basePrice = food.getPrice();

        BigDecimal extraCost = itemDTO.getCustomIngredients().stream()
                .map(c -> ingredientRepository.findById(c.getIngredientId())
                        .orElseThrow(() -> new RuntimeException("Ingredient not found: " + c.getIngredientId()))
                        .getExtraCost())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return basePrice.add(extraCost).multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
    }



    private void validateOrderDTO(OrderDTO orderDTO) {
        if (orderDTO == null || orderDTO.getOrderItems() == null || orderDTO.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("Order items cannot be null or empty");
        }

        for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {

            if ("DRINK".equalsIgnoreCase(itemDTO.getItemType())) {
                if (itemDTO.getDrinkId() == null) {
                    throw new IllegalArgumentException("Drink ID cannot be null for DRINK item");
                }
                continue;
            }

            if (itemDTO.getFoodId() == null) {
                throw new IllegalArgumentException("Food ID cannot be null for FOOD item");
            }
        }
    }


    private void processDelivery(OrderDTO orderDTO, Order order) {
            if (orderDTO.getDeliveryDTO() != null && orderDTO.getDeliveryDTO().getDeliveryAddress() != null) {
                Delivery delivery = new Delivery();
                delivery.setOrder(order);
                delivery.setDeliveryAddress(orderDTO.getDeliveryDTO().getDeliveryAddress());
                delivery.setDeliveryTime(orderDTO.getDeliveryDTO().getDeliveryTime());
                delivery.setStatus(DeliveryStatus.SCHEDULED);
                order.setDelivery(delivery);
                deliveryRepository.save(delivery);
            }
        }

    private void updateOrderDelivery(Order order, DeliveryDTO deliveryDTO) {
        if (deliveryDTO != null) {
            Delivery delivery = order.getDelivery() != null ?
                    order.getDelivery() :
                    new Delivery();

            delivery.setOrder(order);
            delivery.setDeliveryAddress(deliveryDTO.getDeliveryAddress());
            delivery.setDeliveryTime(deliveryDTO.getDeliveryTime());
            delivery.setStatus(DeliveryStatus.valueOf(deliveryDTO.getStatus()));

            if (order.getDelivery() == null) {
                deliveryRepository.save(delivery);
            }
        } else if (order.getDelivery() != null) {
            deliveryRepository.delete(order.getDelivery());
        }
    }

    private OrderDTO buildOrderResponseDTO(Order order) {
        OrderDTO dto = orderMapper.toDTO(order);


        dto.setDeliveryDTO(order.getDelivery() != null ?
                deliveryMapper.toDTO(order.getDelivery()) :
                createNotScheduledDeliveryDTO());
        return dto;
    }

    @Override
    @Transactional
    public void confirmGuestPayment(String paymentIntentId, GuestOrderDTO guestOrderDTO) {
        log.info("🔐 OrderService: Confirming guest payment for paymentIntent: {}", paymentIntentId);

        try {
            Payment payment = paymentRepository.findByPaymentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found for intentId: " + paymentIntentId));

            Optional<Order> existingOrderOpt = orderRepository
                    .findFirstByGuestTokenAndStatusOrderByCreatedAtDesc(guestOrderDTO.getGuestToken(), OrderStatus.PENDING);

            if (existingOrderOpt.isPresent()) {
                Order guestOrder = existingOrderOpt.get();

                log.info("🎯 Found existing order {} for guestToken: {}", guestOrder.getId(), guestOrderDTO.getGuestToken());

                guestOrder.setStatus(OrderStatus.CONFIRMED);
                guestOrder.setPaymentStatus(PaymentStatus.PAID);
                guestOrder.setUpdatedAt(LocalDateTime.now());

                payment.setOrder(guestOrder);
                payment.setStatus(PaymentStatus.PAID);
                paymentRepository.save(payment);

                log.info("✅ OrderService: Successfully confirmed order {} for paymentIntent: {}",
                        guestOrder.getId(), paymentIntentId);

            } else {
                log.error("❌ No pending order found for guestToken: {}", guestOrderDTO.getGuestToken());
                throw new RuntimeException("No pending order found for guest token: " + guestOrderDTO.getGuestToken());
            }

        } catch (Exception e) {
            log.error("❌ OrderService: Error confirming guest payment: {}", e.getMessage());
            throw new RuntimeException("Failed to confirm guest payment: " + e.getMessage());
        }
    }

    private DeliveryDTO createNotScheduledDeliveryDTO() {
        DeliveryDTO dto = new DeliveryDTO();
        dto.setStatus(DeliveryStatus.NOT_SCHEDULED.name());
        return dto;
    }
    private void clearExistingItemsSafely(Order order) {
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                if (item.getOrderItemIngredients() != null) {
                    orderItemIngredientRepository.deleteAll(item.getOrderItemIngredients());
                    item.getOrderItemIngredients().clear();
                }
            }
            orderItemRepository.deleteAll(order.getOrderItems());
            order.getOrderItems().clear();
        }
    }

    private List<OrderItemIngredient> processIngredients(OrderItem orderItem, List<OrderItemIngredientDTO> ingredientDTOs) {
        return ingredientDTOs.stream()
                .map(ingDTO -> {
                    OrderItemIngredient ingredient = new OrderItemIngredient();
                    ingredient.setOrderItem(orderItem);
                    ingredient.setIngredient(ingredientRepository.findById(ingDTO.getIngredientId())
                            .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with ID: " + ingDTO.getIngredientId())));
                    ingredient.setExtraCost(ingDTO.getExtraCost());

                    ingredient.setQuantity(ingDTO.getQuantity());
                    return ingredient;
                })
                .collect(Collectors.toList());
    }

    private OrderItemIngredient createOrderItemIngredient(OrderItemIngredientDTO ingDTO) {
        OrderItemIngredient ingredient = orderItemIngredientMapper.toEntity(ingDTO);
        ingredient.setIngredient(ingredientRepository.findById(ingDTO.getIngredientId())
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found")));
        return ingredient;
    }
    private void clearExistingOrderItems(Order order) {

        if (order.getOrderItems() != null) {
            order.getOrderItems().forEach(item -> {
                if (item.getOrderItemIngredients() != null) {
                    orderItemIngredientRepository.deleteAll(item.getOrderItemIngredients());
                }
                orderItemRepository.delete(item);
            });
            order.getOrderItems().clear();
        }

    }
    private void updateOrderItems(Order order, List<OrderItemDTO> itemDTOs) {

        if (order.getOrderItems() != null) {
            order.getOrderItems().forEach(item -> {
                if (item.getOrderItemIngredients() != null) {
                    orderItemIngredientRepository.deleteAll(item.getOrderItemIngredients());
                }
                orderItemRepository.delete(item);
            });
            order.getOrderItems().clear();
        }


        List<OrderItem> newItems = new ArrayList<>();

        for (OrderItemDTO itemDTO : itemDTOs) {

            if ("DRINK".equalsIgnoreCase(itemDTO.getItemType())) {

                SoftDrink drink = softDrinkRepository.findById(itemDTO.getDrinkId())
                        .orElseThrow(() -> new ResourceNotFoundException("Drink not found"));

                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setDrink(drink);
                item.setItemType(OrderItem.ItemType.DRINK);
                item.setQuantity(itemDTO.getQuantity());

                BigDecimal price = drink.getPrice();

                if (itemDTO.getSize() != null) {
                    price = price.multiply(getSizeMultiplier(itemDTO.getSize()));
                }


                item.setPrice(price.multiply(BigDecimal.valueOf(itemDTO.getQuantity())));

                Map<String, String> customizations = new HashMap<>();
                if (itemDTO.getSize() != null) {
                    customizations.put("size", itemDTO.getSize());
                }
                if (itemDTO.getIceOption() != null) {
                    customizations.put("ice", itemDTO.getIceOption());
                }


                orderItemRepository.save(item);
                newItems.add(item);
                continue;
            }

            Food food = foodRepository.findById(itemDTO.getFoodId())
                    .orElseThrow(() -> new ResourceNotFoundException("Food not found"));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setFood(food);
            item.setItemType(OrderItem.ItemType.FOOD);
            item.setQuantity(itemDTO.getQuantity());

            BigDecimal ingredientTotal = BigDecimal.ZERO;
            List<OrderItemIngredient> ingredients = new ArrayList<>();

            if (itemDTO.getCustomIngredients() != null) {
                for (OrderItemIngredientDTO ingDTO : itemDTO.getCustomIngredients()) {
                    Ingredient ing = ingredientRepository.findById(ingDTO.getIngredientId())
                            .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));

                    OrderItemIngredient oi = new OrderItemIngredient();
                    oi.setOrderItem(item);
                    oi.setIngredient(ing);
                    oi.setQuantity(ingDTO.getQuantity());
                    oi.setExtraCost(ing.getExtraCost());

                    ingredientTotal = ingredientTotal.add(
                            ing.getExtraCost().multiply(BigDecimal.valueOf(ingDTO.getQuantity()))
                    );

                    ingredients.add(oi);
                }
            }

            BigDecimal price = food.getPrice()
                    .multiply(BigDecimal.valueOf(itemDTO.getQuantity()))
                    .add(ingredientTotal);

            item.setPrice(price);
            item.setOrderItemIngredients(ingredients);

            orderItemRepository.save(item);
            orderItemIngredientRepository.saveAll(ingredients);

            newItems.add(item);
        }

        order.setOrderItems(newItems);
        recalculateOrderTotal(order);
    }


    private OrderItem createOrderItem(OrderItemDTO itemDTO) {
        OrderItem item = orderItemMapper.toEntity(itemDTO);

        Food food = foodRepository.findById(itemDTO.getFoodId())
                .orElseThrow(() -> new ResourceNotFoundException("Food not found"));
        item.setFood(food);

        if (itemDTO.getPrice() == null) {
            item.setPrice(food.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
        }

        if (itemDTO.getCustomIngredients() != null) {
            List<OrderItemIngredient> ingredients = itemDTO.getCustomIngredients().stream()
                    .map(this::createOrderItemIngredient)
                    .peek(ing -> ing.setOrderItem(item))
                    .collect(Collectors.toList());
            item.setOrderItemIngredients(ingredients);
        }

        return item;
    }
    private void recalculateOrderTotal(Order order) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : order.getOrderItems()) {
            subtotal = subtotal.add(item.getPrice());
        }

        BigDecimal taxRate = new BigDecimal("0.08");
        BigDecimal taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        order.setTotalPrice(subtotal.add(taxAmount));
    }


    private void setOrderItemCustomizations(OrderItem orderItem, Map<String, String> customizations) {
        try {
            if (customizations != null && !customizations.isEmpty()) {
                String customizationsJson = objectMapper.writeValueAsString(customizations);

                try {

                    java.lang.reflect.Method setCustomizationsMethod = OrderItem.class.getMethod("setCustomizations", String.class);
                    setCustomizationsMethod.invoke(orderItem, customizationsJson);
                } catch (NoSuchMethodException e) {
                    log.warn("OrderItem.setCustomizations() method not found. Customizations not saved.");
                }
            }
        } catch (Exception e) {
            log.error("Error setting drink customizations", e);
        }
    }
}
