package com.resturant.service;

import com.resturant.dto.OrderItemDTO;
import com.resturant.dto.OrderItemIngredientDTO;
import com.resturant.entity.*;
import com.resturant.mapper.OrderItemIngredientMapper;
import com.resturant.mapper.OrderItemMapper;
import com.resturant.repository.FoodRepository;
import com.resturant.repository.IngredientRepository;
import com.resturant.repository.OrderItemRepository;
import com.resturant.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class OrderItemServiceImpl implements OrderItemService{

    @Autowired
    OrderRepository orderRepository;
    @Autowired
    OrderItemRepository orderItemRepository;
    @Autowired
    FoodRepository foodRepository;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    OrderItemMapper orderItemMapper;
    @Autowired
    OrderItemIngredientMapper orderItemIngredientMapper;
    @Autowired
    OrderItemIngredientService orderItemIngredientService;


    @Transactional
    @Override
    public OrderItemDTO createOrderItem(OrderItemDTO orderItemDTO, Order order) {


       if (order == null || order.getId() == null) {
            throw new IllegalArgumentException("Order must be persisted and cannot be null");
        }
        if (orderItemDTO == null) {
            throw new IllegalArgumentException("OrderItemDTO cannot be null");
        }

        // 1. Fetch food
        Food food = foodRepository.findByName(orderItemDTO.getFoodName())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Food not found with name: " + orderItemDTO.getFoodName()));

        // 2. Create and configure OrderItem
        OrderItem orderItem = new OrderItem();
        orderItem.setFood(food);
        orderItem.setOrder(order);
        orderItem.setQuantity(orderItemDTO.getQuantity());

        // 4. Process ingredients
        List<OrderItemIngredient> ingredients = new ArrayList<>();
        if (orderItemDTO.getCustomIngredients() != null) {
            for (OrderItemIngredientDTO ingredientDTO : orderItemDTO.getCustomIngredients()) {
                Ingredient ingredient = ingredientRepository.findById(ingredientDTO.getIngredientId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Ingredient not found with ID: " + ingredientDTO.getIngredientId()));

                OrderItemIngredient orderItemIngredient = new OrderItemIngredient();
                orderItemIngredient.setIngredient(ingredient);
                orderItemIngredient.setQuantity(ingredientDTO.getQuantity());
                orderItemIngredient.setExtraCost(ingredient.getExtraCost());
                orderItemIngredient.setOrderItem(orderItem); // Critical bidirectional link

                ingredients.add(orderItemIngredient);
            }
        }

        // 5. Set the complete list at once to maintain consistency
        orderItem.setOrderItemIngredients(ingredients);

        // 6. Calculate price
        BigDecimal basePrice = food.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
        BigDecimal extraCost = ingredients.stream()
                .map(oi -> oi.getExtraCost().multiply(BigDecimal.valueOf(oi.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orderItem.setPrice(basePrice.add(extraCost));

        // 7. Save (cascade will persist ingredients)
        OrderItem savedOrderItem = orderItemRepository.save(orderItem);

        // 8. Return DTO with all data
        OrderItemDTO resultDTO = orderItemMapper.toDTO(savedOrderItem);
        resultDTO.setCustomIngredients(orderItemDTO.getCustomIngredients());
        return resultDTO;

    }
    @Override
    public List<OrderItemDTO> createOrderItems(List<OrderItemDTO> itemDTOs, Order order) {
        return itemDTOs.stream()
                .map(dto -> createOrderItem(dto, order))
                .collect(Collectors.toList());
    }

    @Override
    public OrderItemDTO getOrderItemById(Long id) {
        return null;
    }

    @Override
    public List<OrderItemDTO> getAllOrderItem() {

        return orderItemMapper.toDTOList(orderItemRepository.findAll());
    }

    @Override
    public OrderItemDTO updateOrderItem(Long id, OrderItemDTO orderItemDTO) {
        return null;
    }

    @Override
    public void deleteOrderItem(Long id) {
        orderRepository.deleteById(id);

    }
}
