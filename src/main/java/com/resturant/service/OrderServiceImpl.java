package com.resturant.service;


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
import org.springframework.security.crypto.password.PasswordEncoder;
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
    OrderItemIngredientMapper orderItemIngredientMapper;
    private final GuestUserService guestUserService;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    @Override
    public OrderDTO placeOrder(OrderDTO orderDTO) {
        if (orderDTO == null)
            throw new IllegalArgumentException("OrderDTO cannot be null");

        if (orderDTO.isGuest())
            throw new IllegalArgumentException("Use placeGuestOrder for guest users");

        // 1️⃣ Find user by email
        User user = userRepository.findByEmail(orderDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found: " + orderDTO.getEmail()));

        // 2️⃣ Create and save order first to get an ID
        Order order = new Order();
        order.setUser(user);
        order.setIsGuest(false);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setSpecialInstructions(orderDTO.getSpecialInstructions());
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setCreatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        // 3️⃣ Process order items - CALCULATE EVERYTHING FROM DATABASE PRICES
        List<OrderItem> savedItems = new ArrayList<>();
        BigDecimal orderSubtotal = BigDecimal.ZERO;

        System.out.println("=== BACKEND SECURE CALCULATION START ===");

        for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {
            // Get food price FROM DATABASE (ignore frontend price)
            Food food = foodRepository.findById(itemDTO.getFoodId())
                    .orElseThrow(() -> new RuntimeException("Food not found: " + itemDTO.getFoodId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setFood(food);
            orderItem.setQuantity(itemDTO.getQuantity());

            // Calculate base price FROM DATABASE
            BigDecimal basePrice = food.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            BigDecimal ingredientsTotal = BigDecimal.ZERO;

            System.out.println("--- Processing: " + food.getName() + " ---");
            System.out.println("DB Food Price: " + food.getPrice());
            System.out.println("Quantity: " + itemDTO.getQuantity());
            System.out.println("Base Price: " + basePrice);

            // Process ingredients - GET PRICES FROM DATABASE
            List<OrderItemIngredient> itemIngredients = new ArrayList<>();
            if (itemDTO.getCustomIngredients() != null) {
                for (OrderItemIngredientDTO ingDTO : itemDTO.getCustomIngredients()) {
                    if (ingDTO.getIngredientId() == null) {
                        throw new IllegalArgumentException("Ingredient ID cannot be null");
                    }

                    // Get ingredient price FROM DATABASE
                    Ingredient ingredient = ingredientRepository.findById(ingDTO.getIngredientId())
                            .orElseThrow(() -> new RuntimeException("Ingredient not found: " + ingDTO.getIngredientId()));

                    OrderItemIngredient oi = new OrderItemIngredient();
                    oi.setOrderItem(orderItem);
                    oi.setIngredient(ingredient);
                    oi.setQuantity(ingDTO.getQuantity() != 0 ? ingDTO.getQuantity() : 1);
                    oi.setExtraCost(ingredient.getExtraCost()); // Use DB price

                    itemIngredients.add(oi);

                    // Calculate ingredient cost
                    BigDecimal ingredientCost = ingredient.getExtraCost().multiply(BigDecimal.valueOf(oi.getQuantity()));
                    ingredientsTotal = ingredientsTotal.add(ingredientCost);

                    System.out.println("Ingredient: " + ingredient.getName());
                    System.out.println("  DB Extra Cost: " + ingredient.getExtraCost());
                    System.out.println("  Quantity: " + oi.getQuantity());
                    System.out.println("  Cost: " + ingredientCost);
                }
            }

            System.out.println("Total Ingredients Cost: " + ingredientsTotal);

            // CALCULATE FINAL ITEM PRICE FROM DATABASE PRICES (ignore frontend price)
            BigDecimal backendItemPrice = basePrice.add(ingredientsTotal);
            orderItem.setPrice(backendItemPrice); // Use backend calculation

            System.out.println("Backend Calculated Price: " + backendItemPrice);
            System.out.println("Frontend Sent Price: " + itemDTO.getPrice()); // Just for comparison

            // Save order item
            OrderItem savedOrderItem = orderItemRepository.save(orderItem);

            // Save ingredients
            if (!itemIngredients.isEmpty()) {
                itemIngredients.forEach(ing -> ing.setOrderItem(savedOrderItem));
                orderItemIngredientRepository.saveAll(itemIngredients);
                savedOrderItem.setOrderItemIngredients(itemIngredients);
            }

            savedItems.add(savedOrderItem);

            // Add to subtotal (INSIDE THE LOOP - CRITICAL FIX)
            orderSubtotal = orderSubtotal.add(backendItemPrice);
            System.out.println("Current Subtotal: " + orderSubtotal);
            System.out.println("--- End: " + food.getName() + " ---");
        }

        System.out.println("FINAL SUBTOTAL: " + orderSubtotal);

        // 4️⃣ Calculate tax and total - USE BACKEND CALCULATION
        BigDecimal taxRate = new BigDecimal("0.08");
        BigDecimal taxAmount = orderSubtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal backendTotalPrice = orderSubtotal.add(taxAmount);

        System.out.println("Tax (8%): " + taxAmount);
        System.out.println("BACKEND TOTAL: " + backendTotalPrice);

        // 5️⃣ Security Validation - Compare with frontend (for fraud detection)
        if (orderDTO.getTotalPrice() != null) {
            BigDecimal frontendTotalPrice = orderDTO.getTotalPrice();
            BigDecimal difference = frontendTotalPrice.subtract(backendTotalPrice).abs();
            BigDecimal tolerance = new BigDecimal("0.01");

            System.out.println("=== SECURITY VALIDATION ===");
            System.out.println("Frontend Total: " + frontendTotalPrice);
            System.out.println("Backend Total: " + backendTotalPrice);
            System.out.println("Difference: " + difference);

            if (difference.compareTo(tolerance) > 0) {
                System.out.println("⚠️  WARNING: Price discrepancy detected!");
                // Log this for security monitoring
            }
            System.out.println("=== END VALIDATION ===");
        }

        // 6️⃣ Always use backend calculation for security
        order.setTotalPrice(backendTotalPrice);
        order = orderRepository.save(order);

        System.out.println("✅ ORDER COMPLETED - Total: " + order.getTotalPrice());
        System.out.println("=== BACKEND SECURE CALCULATION END ===");

        return orderMapper.toDTO(order);
    }
    public OrderDTO mapGuestOrderToOrderDTO(GuestOrderDTO guestOrderDTO) {
        if (guestOrderDTO == null) {
            throw new IllegalArgumentException("GuestOrderDTO cannot be null");
        }

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setUserName(guestOrderDTO.getGuestName());       // map guestName
        orderDTO.setEmail(guestOrderDTO.getGuestEmail());         // map guestEmail
        orderDTO.setOrderItems(guestOrderDTO.getOrderItemDTOS()); // map order items
        orderDTO.setSpecialInstructions(guestOrderDTO.getSpecialInstructions());
        orderDTO.setGuest(true);                                // mark as guest
        orderDTO.setPaymentStatus(PaymentStatus.PENDING);         // default

        return orderDTO;
    }

    @Transactional
    @Override
    public OrderDTO placeGuestOrder(GuestOrderDTO guestOrderDTO) {
        if (guestOrderDTO == null) throw new IllegalArgumentException("Guest order cannot be null");

        // 1️⃣ Prepare Order entity
        Order order = new Order();
        order.setIsGuest(true);
        order.setGuestName(guestOrderDTO.getGuestName());
        order.setGuestEmail(guestOrderDTO.getGuestEmail());
        order.setStatus(OrderStatus.PENDING);
        order.setSpecialInstructions(guestOrderDTO.getSpecialInstructions());

        // 2️⃣ Prepare OrderItems
        BigDecimal orderTotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemDTO itemDTO : guestOrderDTO.getOrderItemDTOS()) {
            Food food = foodRepository.findById(itemDTO.getFoodId())
                    .orElseThrow(() -> new RuntimeException("Food not found: " + itemDTO.getFoodId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setFood(food);
            orderItem.setQuantity(itemDTO.getQuantity());

            // Map extra ingredients
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

            // Calculate total price for this order item
            BigDecimal totalPrice = (food.getPrice().add(extraCost)).multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            orderItem.setPrice(totalPrice);

            orderItems.add(orderItem);
            orderTotal = orderTotal.add(totalPrice);
        }

        order.setOrderItems(orderItems);
        order.setTotalPrice(orderTotal);

        // 3️⃣ Save order
        order = orderRepository.save(order);

        // 4️⃣ Map to DTO
        return orderMapper.toDTO(order);
    }


    @Transactional
    @Override
    public OrderDTO payForOrder(Long orderId, PaymentRequestDTO paymentRequest) {
        // 1️⃣ Fetch the order from DB using the path variable
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // 1a️⃣ Ensure totalPrice is not null
        if (order.getTotalPrice() == null) {
            throw new RuntimeException("Order total price is not calculated yet!");
        }

        // 2️⃣ Build a safe PaymentRequestDTO from order data
        PaymentRequestDTO stripeRequest = new PaymentRequestDTO();
        stripeRequest.setOrderId(orderId);
        stripeRequest.setAmount(order.getTotalPrice().doubleValue());   // ✅ safe now
        stripeRequest.setPaymentMethod(paymentRequest.getPaymentMethod());
        stripeRequest.setGuestEmail(paymentRequest.getGuestEmail());
        stripeRequest.setGuestName(paymentRequest.getGuestName());

        // 3️⃣ Initiate payment with Stripe
        PaymentResponseDTO paymentResponse = paymentService.initiatePayment(stripeRequest);

        // 4️⃣ Confirm payment (synchronously here, or via webhook)
        paymentService.confirmPayment(paymentResponse.getPaymentIntentId());

        // 5️⃣ Update order status
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        return orderMapper.toDTO(order);
    }





    @Override
    public PaymentResponseDTO createGuestPaymentIntent(GuestOrderDTO guestOrderDTO) throws StripeException {
        long amountInCents = (long) (guestOrderDTO.getTotalAmount() * 100); // convert to cents

        try{
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amountInCents);
        params.put("currency", "usd");

        Map<String, Object> autoPaymentMethods = new HashMap<>();
        autoPaymentMethods.put("enabled", true);
        params.put("automatic_payment_methods", autoPaymentMethods);

        PaymentIntent paymentIntent = PaymentIntent.create(params);
            Payment payment = new Payment();
            payment.setPaymentId(paymentIntent.getId());
            payment.setAmount(guestOrderDTO.getTotalAmount());
            payment.setStatus(PaymentStatus.PENDING); // Will be updated to SUCCESS later
            payment.setPaymentDate(LocalDateTime.now());
            // Don't set order yet - order will be created in createGuestOrderAfterPayment
            paymentRepository.save(payment);



            return new PaymentResponseDTO(
                paymentIntent.getClientSecret(),
                paymentIntent.getId(),
                paymentIntent.getStatus()
        );

    } catch (Exception e) {
        throw new RuntimeException("Error creating payment intent", e);
    }
    }
    public OrderDTO createGuestOrderAfterPayment(GuestOrderDTO guestOrderDTO, String paymentIntentId) {
        // 1. Create the guest order
        Order guestOrder = new Order();
        guestOrder.setIsGuest(true);
        guestOrder.setGuestEmail(guestOrderDTO.getGuestEmail());
        guestOrder.setGuestName(guestOrderDTO.getGuestName());
        guestOrder.setSpecialInstructions(guestOrderDTO.getSpecialInstructions());
        guestOrder.setStatus(OrderStatus.CONFIRMED);
        guestOrder.setPaymentStatus(PaymentStatus.PAID); // FIXED: Changed from SUCCESS to PAID
        guestOrder.setOrderNumber(UUID.randomUUID().toString());
        guestOrder.setCreatedAt(LocalDateTime.now());

        // 2. Process order items (similar to placeOrder method)
        BigDecimal orderSubtotal = BigDecimal.ZERO; // Subtotal before tax
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemDTO itemDTO : guestOrderDTO.getOrderItemDTOS()) {
            Food food = foodRepository.findById(itemDTO.getFoodId())
                    .orElseThrow(() -> new RuntimeException("Food not found: " + itemDTO.getFoodId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(guestOrder);
            orderItem.setFood(food);
            orderItem.setQuantity(itemDTO.getQuantity());

            // Calculate base price
            BigDecimal basePrice = food.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            BigDecimal extras = BigDecimal.ZERO;

            // Map extra ingredients
            List<OrderItemIngredient> ingredients = new ArrayList<>();
            if (itemDTO.getCustomIngredients() != null) {
                for (OrderItemIngredientDTO ciDTO : itemDTO.getCustomIngredients()) {
                    Ingredient ingredient = ingredientRepository.findById(ciDTO.getIngredientId())
                            .orElseThrow(() -> new RuntimeException("Ingredient not found: " + ciDTO.getIngredientId()));

                    OrderItemIngredient oi = new OrderItemIngredient();
                    oi.setOrderItem(orderItem);
                    oi.setIngredient(ingredient);
                    oi.setQuantity(ciDTO.getQuantity() != 0 ? ciDTO.getQuantity() : 1);
                    oi.setExtraCost(ingredient.getExtraCost() != null ? ingredient.getExtraCost() : BigDecimal.ZERO);

                    ingredients.add(oi);
                    extras = extras.add(oi.getExtraCost().multiply(BigDecimal.valueOf(oi.getQuantity())));
                }
            }

            orderItem.setOrderItemIngredients(ingredients);

            // Calculate total price for this order item (base + extras)
            BigDecimal itemPrice = basePrice.add(extras);
            orderItem.setPrice(itemPrice);

            orderItems.add(orderItem);
            orderSubtotal = orderSubtotal.add(itemPrice);
        }

        guestOrder.setOrderItems(orderItems);

        // 3. Calculate tax and total price (ADDED TAX CALCULATION)
        BigDecimal taxRate = new BigDecimal("0.08"); // 8% tax - use same rate as placeOrder
        BigDecimal taxAmount = orderSubtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPrice = orderSubtotal.add(taxAmount);

        guestOrder.setTotalPrice(totalPrice);

        // 4. Save order
        guestOrder = orderRepository.save(guestOrder);

        // 5. Link payment
        Payment payment = paymentRepository.findByPaymentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentIntentId));
        payment.setOrder(guestOrder);
        paymentRepository.save(payment);

        return orderMapper.toDTO(guestOrder);
    }


    @Override
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Order not found with Order Id: " + id));

        return orderMapper.toDTO(order);
    }

    @Override
    public List<OrderDTO> findByUserEmail(String email) {
        List<Order> orders = orderRepository.findByUserEmail(email);
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


        // 3. Process order items
        updateOrderItems(existingOrder, orderDTO.getOrderItems());

        // 4. Handle delivery
        updateOrderDelivery(existingOrder, orderDTO.getDeliveryDTO());

        // 5. Recalculate total price
        recalculateOrderTotal(existingOrder);

        // 6. Save and return
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
        order.setUpdatedAt(LocalDateTime.now()); // if you track time

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

    // ========== HELPER METHODS ==========

    private BigDecimal calculateOrderTotal(OrderDTO orderDTO) {
        return orderDTO.getOrderItems().stream()
                .map(itemDTO -> {
                    // Get food price (use provided price or fetch from DB)
                    BigDecimal itemPrice = itemDTO.getPrice() != null ?
                            itemDTO.getPrice() :
                            foodRepository.findById(itemDTO.getFoodId())
                                    .orElseThrow(() -> new ResourceNotFoundException("Food not found"))
                                    .getPrice();

                    // Calculate base price (price × quantity)
                    BigDecimal basePrice = itemPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

                    // Add ingredients cost
                    if (itemDTO.getCustomIngredients() != null) {
                        for (OrderItemIngredientDTO ingDTO : itemDTO.getCustomIngredients()) {
                            BigDecimal extraCost = ingDTO.getExtraCost() != null ?
                                    ingDTO.getExtraCost() : BigDecimal.ZERO;
                            basePrice = basePrice.add(extraCost.multiply(BigDecimal.valueOf(ingDTO.getQuantity())));
                        }
                    }
                    return basePrice;
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

                    // base price = food price * quantity
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
                                    // Always use DB price, not client-sent
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

//    private void buildAndSaveOrder(OrderDTO orderDTO, Order order) {
//        order.setSpecialInstructions(orderDTO.getSpecialInstructions());
//        order.setStatus(OrderStatus.PENDING);
//        order.setPaymentStatus(PaymentStatus.PENDING);
//        order.setOrderNumber(UUID.randomUUID().toString());
//
//        // Process items
//        List<OrderItem> orderItems = processOrderItems(order, orderDTO);
//        order.setOrderItems(orderItems);
//
//        // Subtotal
//        BigDecimal subtotal = orderItems.stream()
//                .map(OrderItem::getPrice)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        // Tax
//        BigDecimal taxRate = new BigDecimal("0.08"); // 8%
//        BigDecimal tax = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
//
//        // Final total
//        BigDecimal totalPrice = subtotal.add(tax);
//
//        order.setTotalPrice(totalPrice);
//
//        if (orderDTO.isGuest()) {
//            order.setTrackingToken(UUID.randomUUID().toString());
//        }
//
//        orderRepository.save(order);
//        processDelivery(orderDTO, order);
//    }


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

    // --- Validation
    private void validateOrderDTO(OrderDTO orderDTO) {
        if (orderDTO == null || orderDTO.getOrderItems() == null || orderDTO.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("Order items cannot be null or empty");
        }

        for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {
            if (itemDTO.getFoodId() == null) {
                throw new IllegalArgumentException("Food ID cannot be null in order items");
            }
            if (itemDTO.getCustomIngredients() != null) {
                for (OrderItemIngredientDTO ingDTO : itemDTO.getCustomIngredients()) {
                    if (ingDTO.getIngredientId() == null) {
                        throw new IllegalArgumentException("Ingredient ID cannot be null");
                    }
                }
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

//        BigDecimal totalPrice = order.getTotalPrice(); // already calculated
//        dto.setTotalPrice(totalPrice);

        dto.setDeliveryDTO(order.getDelivery() != null ?
                deliveryMapper.toDTO(order.getDelivery()) :
                createNotScheduledDeliveryDTO());
        return dto;
    }

    private DeliveryDTO createNotScheduledDeliveryDTO() {
        DeliveryDTO dto = new DeliveryDTO();
        dto.setStatus(DeliveryStatus.NOT_SCHEDULED.name());
        return dto;
    }
    private void clearExistingItemsSafely(Order order) {
        // Properly clear bidirectional relationships
        if (order.getOrderItems() != null) {
            order.getOrderItems().forEach(item -> {
                if (item.getOrderItemIngredients() != null) {
                    item.getOrderItemIngredients().forEach(ing -> ing.setOrderItem(null));
                    item.getOrderItemIngredients().clear();
                }
                item.setOrder(null);
            });
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
        order.getOrderItems().forEach(item -> {
            orderItemIngredientRepository.deleteAll(item.getOrderItemIngredients());
            orderItemRepository.delete(item);
        });
        order.getOrderItems().clear();
    }
    private void updateOrderItems(Order order, List<OrderItemDTO> itemDTOs) {
        // Clear existing items properly
        List<OrderItem> itemsToDelete = new ArrayList<>(order.getOrderItems());
        order.getOrderItems().clear();
        itemsToDelete.forEach(item -> {
            orderItemIngredientRepository.deleteAll(item.getOrderItemIngredients());
            orderItemRepository.delete(item);
        });

        // Add new items
        if (itemDTOs != null) {
            itemDTOs.forEach(itemDTO -> {
                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setFood(foodRepository.findById(itemDTO.getFoodId())
                        .orElseThrow(() -> new ResourceNotFoundException("Food not found")));
                item.setQuantity(itemDTO.getQuantity());
                item.setPrice(itemDTO.getPrice() != null ?
                        itemDTO.getPrice() :
                        item.getFood().getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));

                // Process ingredients
                if (itemDTO.getCustomIngredients() != null) {
                    item.setOrderItemIngredients(
                            itemDTO.getCustomIngredients().stream()
                                    .map(ingDTO -> {
                                        OrderItemIngredient ing = new OrderItemIngredient();
                                        ing.setOrderItem(item);
                                        ing.setIngredient(ingredientRepository.findById(ingDTO.getIngredientId())
                                                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found")));
//                                        ing.setExtraCost(ingDTO.getExtraCost());
                                        ing.setExtraCost( ingDTO.getExtraCost() != null ? ingDTO.getExtraCost() : BigDecimal.ZERO
                                        );
                                        ing.setQuantity(ingDTO.getQuantity());
                                        return ing;
                                    })
                                    .collect(Collectors.toList())
                    );
                }
                order.getOrderItems().add(item);
            });
        }
    }


    private OrderItem createOrderItem(OrderItemDTO itemDTO) {
        OrderItem item = orderItemMapper.toEntity(itemDTO);

        // Set food
        Food food = foodRepository.findById(itemDTO.getFoodId())
                .orElseThrow(() -> new ResourceNotFoundException("Food not found"));
        item.setFood(food);

        // Calculate price if not provided
        if (itemDTO.getPrice() == null) {
            item.setPrice(food.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
        }

        // Process ingredients
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
        BigDecimal total = order.getOrderItems().stream()
                .map(OrderItem::getPrice)
//                .map(item -> {
//                    BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
//
//                    if (item.getOrderItemIngredients() != null) {
//                        BigDecimal ingredientsTotal = item.getOrderItemIngredients().stream()
//                                .map(ing -> ing.getExtraCost().multiply(BigDecimal.valueOf(ing.getQuantity())))
//
////                                .map(ing -> ing.getExtraCost().multiply(BigDecimal.valueOf(ing.getQuantity())))
//                                .reduce(BigDecimal.ZERO, BigDecimal::add);
//                        itemTotal = itemTotal.add(ingredientsTotal);
//                    }
//
//                    return itemTotal;
//                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        order.setTotalPrice(total);
    }

}
