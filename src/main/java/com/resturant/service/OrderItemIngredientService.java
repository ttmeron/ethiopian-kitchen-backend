package com.resturant.service;

import com.resturant.dto.OrderItemIngredientDTO;
import com.resturant.entity.OrderItemIngredient;

import java.util.List;

public interface OrderItemIngredientService {

    OrderItemIngredientDTO createOrderItemIngredient(OrderItemIngredientDTO orderItemIngredientDTO);
    List<OrderItemIngredientDTO> findByOrderItemId(Long orderItemId);
    List<OrderItemIngredientDTO> getAllOrderItemIngredient();
    void deleteOrderItemIngredient(Long id);
    public OrderItemIngredient toEntity(OrderItemIngredientDTO dto);
}
