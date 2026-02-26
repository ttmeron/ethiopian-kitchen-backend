package com.resturant.service;

import com.resturant.dto.*;
import com.resturant.dto.response.GuestOrderResponseDTO;
import com.resturant.entity.*;
import com.resturant.mapper.OrderMapper;
import com.resturant.mapper.PaymentMapper;
import com.resturant.repository.*;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentRepository paymentRepository;
    private final FoodRepository foodRepository;
    private final IngredientRepository ingredientRepository;

    private final SoftDrinkRepository softDrinkRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
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

            String clientSecret = intent.getClientSecret();
            Payment payment = paymentMapper.toEntity(requestDTO, intent.getId(), order.getTotalPrice());

            payment.setOrder(order);

            payment.setStatus(PaymentStatus.PENDING);
            payment.setPaymentMethod("stripe");

            paymentRepository.save(payment);
            PaymentResponseDTO responseDto = paymentMapper.toDto(payment, clientSecret);
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
        params.put("amount", (long) (request.getAmount() * 100));
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

    @Override
    @Transactional
    public void confirmGuestPayment(String paymentIntentId, GuestOrderDTO guestOrderDTO) {
        log.info("🔐 PaymentService: Confirming guest payment for paymentIntent: {}", paymentIntentId);


        try {
            Payment payment = paymentRepository.findByPaymentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found for intentId: " + paymentIntentId));

            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

            String paymentMethodType = "unknown";

            if (intent.getPaymentMethodTypes() != null && !intent.getPaymentMethodTypes().isEmpty()) {
                paymentMethodType = intent.getPaymentMethodTypes().get(0);
            }

            if (intent.getPaymentMethod() != null) {
                try {
                    com.stripe.model.PaymentMethod pm = com.stripe.model.PaymentMethod.retrieve(intent.getPaymentMethod());
                    paymentMethodType = pm.getType();

                } catch (Exception e) {
                    log.warn("Could not retrieve payment method details: {}", e.getMessage());
                }
            }

            payment.setPaymentMethod(paymentMethodType);
            payment.setStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);

            Order order = payment.getOrder();
            if (order == null) {
                throw new RuntimeException("No order found for payment: " + paymentIntentId);
            }

            order.setGuestName(guestOrderDTO.getGuestName());
            order.setGuestEmail(guestOrderDTO.getGuestEmail());
            order.setStatus(OrderStatus.CONFIRMED);
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setSpecialInstructions(guestOrderDTO.getSpecialInstructions());
            order.setTotalPrice(guestOrderDTO.getTotalAmount());

            String orderNumber = "ORD-" + System.currentTimeMillis() + "-" +
                    UUID.randomUUID().toString().substring(0, 8).toUpperCase();


            order.setOrderNumber(orderNumber);

            if (order.getOrderItems() == null) {
                order.setOrderItems(new ArrayList<>());
            } else {
                order.getOrderItems().clear();
            }

            for (OrderItemDTO itemDto : guestOrderDTO.getOrderItemDTOS()) {

                OrderItem orderItem = new OrderItem();
                orderItem.setItemType(OrderItem.ItemType.valueOf(itemDto.getItemType()));
                orderItem.setQuantity(itemDto.getQuantity());
                orderItem.setPrice(itemDto.getPrice());

                // Set food or drink based on item type
                if (itemDto.getItemType().equals("FOOD") && itemDto.getFoodId() != null) {
                    Food food = foodRepository.findById(itemDto.getFoodId())
                            .orElseThrow(() -> new RuntimeException("Food not found: " + itemDto.getFoodId()));
                    orderItem.setFood(food);
                } else if (itemDto.getItemType().equals("DRINK") && itemDto.getDrinkId() != null) {

                    SoftDrink drink = softDrinkRepository.findById(itemDto.getDrinkId())
                             .orElseThrow(() -> new RuntimeException("Drink not found: " + itemDto.getDrinkId()));
                     orderItem.setDrink(drink);

                    orderItem.setSize(itemDto.getSize());
                    orderItem.setIceOption(itemDto.getIceOption());
                    log.info(
                            "🧾 OrderItemDTO → type={}, size={}, ice={}",
                            itemDto.getItemType(),
                            itemDto.getSize(),
                            itemDto.getIceOption()
                    );


                }
                if (itemDto.getCustomIngredients() != null && !itemDto.getCustomIngredients().isEmpty()) {

                    for (OrderItemIngredientDTO ingredientDto : itemDto.getCustomIngredients()) {

                        Ingredient ingredient = ingredientRepository
                                .findById(ingredientDto.getIngredientId())
                                .orElseThrow(() ->
                                        new RuntimeException("Ingredient not found: " + ingredientDto.getIngredientId())
                                );

                        OrderItemIngredient orderItemIngredient = new OrderItemIngredient();
                        orderItemIngredient.setOrderItem(orderItem);
                        orderItemIngredient.setIngredient(ingredient);
                        orderItemIngredient.setQuantity(ingredientDto.getQuantity());
                        orderItemIngredient.setExtraCost(ingredientDto.getExtraCost());

                        orderItem.getOrderItemIngredients().add(orderItemIngredient);
                    }
                }


                orderItem.setOrder(order);
                order.getOrderItems().add(orderItem);
            }

            orderRepository.save(order);

            log.info("✅ PaymentService: Guest payment confirmed for paymentIntent: {}", paymentIntentId);

        } catch (Exception e) {
            log.error("❌ PaymentService: Error confirming guest payment: {}", e.getMessage());
            throw new RuntimeException("Failed to confirm guest payment: " + e.getMessage());
        }
    }
    @Override
    public PaymentResponseDTO createGuestPaymentIntent(GuestOrderDTO guestOrderDTO) {
        log.info("🚀 Entered createGuestPaymentIntent() with guestOrderDTO: {}", guestOrderDTO);


        for (OrderItemDTO item : guestOrderDTO.getOrderItemDTOS()) {
            log.info("💡 OrderItemDTO before payment: type={}, size={}, ice={}, drinkId={}",
                    item.getItemType(), item.getSize(), item.getIceOption(), item.getDrinkId());
        }
        BigDecimal totalAmount = guestOrderDTO.getTotalAmount();
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;

        int amountInCents = totalAmount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        try {
            String guestToken = guestOrderDTO.getGuestToken();
            if (guestToken == null || guestToken.isEmpty()) {
                guestToken = UUID.randomUUID().toString();
                guestOrderDTO.setGuestToken(guestToken);
            }

            Optional<Order> existingOrderOpt = orderRepository.findFirstByGuestTokenAndStatusOrderByCreatedAtDesc(guestToken, OrderStatus.PENDING);
            Order tempOrder;

            if (existingOrderOpt.isPresent()) {
                log.info("🔁 Reusing existing pending order for guestToken: {}", guestToken);
                tempOrder = existingOrderOpt.get();
                tempOrder.setTotalPrice(totalAmount);
                tempOrder.setUpdatedAt(LocalDateTime.now());
            } else {
                log.info("🆕 Creating new pending order for guestToken: {}", guestToken);
                tempOrder = new Order();
                tempOrder.setGuestName(guestOrderDTO.getGuestName());
                tempOrder.setGuestEmail(guestOrderDTO.getGuestEmail());
                tempOrder.setGuestToken(guestToken);
                tempOrder.setIsGuest(true);
                tempOrder.setStatus(OrderStatus.PENDING);
                tempOrder.setPaymentStatus(PaymentStatus.PENDING);
                tempOrder.setTotalPrice(totalAmount);
                tempOrder.setOrderNumber("TEMP-" + UUID.randomUUID());
                tempOrder.setCreatedAt(LocalDateTime.now());
            }

            Order savedOrder = orderRepository.save(tempOrder);

            Map<String, Object> params = new HashMap<>();
            params.put("amount", amountInCents);
            params.put("currency", "usd");
            params.put("payment_method_types", Arrays.asList("card"));

            PaymentIntent intent = PaymentIntent.create(params);

            Payment payment = paymentRepository.findByPaymentId(intent.getId()).orElse(new Payment());
            payment.setPaymentId(intent.getId());
            payment.setOrder(savedOrder);
            payment.setAmount(totalAmount);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setPaymentMethod(null);

            paymentRepository.save(payment);

            PaymentResponseDTO response = new PaymentResponseDTO(
                    intent.getClientSecret(),
                    intent.getId(),
                    intent.getStatus()
            );
            response.setGuestToken(guestToken);
            response.setAmount(totalAmount);

            return response;

        } catch (StripeException e) {
            log.error("❌ Stripe error: {}", e.getMessage());
            throw new RuntimeException("Failed to create guest payment intent: " + e.getMessage());
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
