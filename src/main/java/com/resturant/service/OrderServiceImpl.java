package com.resturant.service;


import com.resturant.dto.DeliveryDTO;
import com.resturant.dto.OrderDTO;
import com.resturant.dto.OrderItemDTO;
import com.resturant.dto.OrderItemIngredientDTO;
import com.resturant.entity.*;
import com.resturant.exception.ResourceNotFoundException;
import com.resturant.mapper.DeliveryMapper;
import com.resturant.mapper.OrderItemIngredientMapper;
import com.resturant.mapper.OrderItemMapper;
import com.resturant.mapper.OrderMapper;
import com.resturant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderRepository orderRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    FoodRepository foodRepository;
    @Autowired
    DeliveryRepository deliveryRepository;
    @Autowired
    IngredientRepository ingredientRepository;
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


    @Transactional
    @Override
    public OrderDTO placeOrder(OrderDTO orderDTO) {


        if (orderDTO == null || orderDTO.getOrderItems() == null || orderDTO.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("Order items cannot be null or empty");
        }
        for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {
            if (itemDTO.getFoodId() == null) {  // Check food ID is present
                throw new IllegalArgumentException("Food ID cannot be null in order items");
            }

            if (itemDTO.getCustomIngredients() != null) {
                for (OrderItemIngredientDTO ingDTO : itemDTO.getCustomIngredients()) {
                    if (ingDTO.getIngredientId() == null) {  // Check ingredient ID
                        throw new IllegalArgumentException("Ingredient ID cannot be null");
                    }
                }
            }
        }

        User user = userRepository.findByEmail(orderDTO.getEmail())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUserName(orderDTO.getUserName());
                    newUser.setEmail(orderDTO.getEmail());
                    return userRepository.save(newUser);
                });

//        Order order = orderMapper.toEntity(orderDTO);
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);


        BigDecimal totalPrice = calculateOrderTotal(orderDTO);
        order.setTotalPrice(totalPrice);

        // Process order items
        List<OrderItem> orderItems = processOrderItems(order, orderDTO);
        order.setOrderItems(orderItems);

        // Save order (cascades to items and ingredients)
        Order savedOrder = orderRepository.save(order);

        // Process delivery if needed
        processDelivery(orderDTO, savedOrder);

        return buildOrderResponseDTO(savedOrder);
        }

    @Override
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Order not found with Order Id: " + id));

        return orderMapper.toDTO(order);
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
    public OrderDTO updateOrder(Long orderId, OrderDTO orderDTO) {        Order existingOrder = orderRepository.findByIdWithRelations(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));


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
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setFood(foodRepository.findById(itemDTO.getFoodId())
                            .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + itemDTO.getFoodId())));
                    orderItem.setQuantity(itemDTO.getQuantity());
                    orderItem.setPrice(itemDTO.getPrice());

                    if (itemDTO.getCustomIngredients() != null) {
                        List<OrderItemIngredient> ingredients = itemDTO.getCustomIngredients().stream()
                                .map(ingDTO -> {
                                    OrderItemIngredient ingredient = new OrderItemIngredient();
                                    ingredient.setIngredient(ingredientRepository.findById(ingDTO.getIngredientId())
                                            .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found")));
                                    ingredient.setExtraCost(ingDTO.getExtraCost());
                                    ingredient.setQuantity(ingDTO.getQuantity());
                                    ingredient.setOrderItem(orderItem);
                                    return ingredient;
                                })
                                .collect(Collectors.toList());
                        orderItem.setOrderItemIngredients(ingredients);
                    }

                    return orderItem;
                })
                .collect(Collectors.toList());
    }

    private void processDelivery(OrderDTO orderDTO, Order order) {
        if (orderDTO.getDeliveryDTO() != null &&
                orderDTO.getDeliveryDTO().getDeliveryAddress() != null) {

            Delivery delivery = new Delivery();
            delivery.setOrder(order);
            delivery.setDeliveryAddress(orderDTO.getDeliveryDTO().getDeliveryAddress());
            delivery.setDeliveryTime(orderDTO.getDeliveryDTO().getDeliveryTime());
            delivery.setStatus(DeliveryStatus.SCHEDULED);
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
                                        ing.setExtraCost(ingDTO.getExtraCost());
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
                .map(item -> {
                    BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

                    if (item.getOrderItemIngredients() != null) {
                        BigDecimal ingredientsTotal = item.getOrderItemIngredients().stream()
                                .map(ing -> ing.getExtraCost().multiply(BigDecimal.valueOf(ing.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        itemTotal = itemTotal.add(ingredientsTotal);
                    }

                    return itemTotal;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(total);
    }

}
