package com.resturant.service;

import com.resturant.dto.OrderItemDTO;
import com.resturant.entity.Order;

import java.util.List;

public interface OrderItemService {

    OrderItemDTO createOrderItem(OrderItemDTO orderItemDTO, Order order);
    List<OrderItemDTO> createOrderItems(List<OrderItemDTO> itemDTOs, Order order);
    OrderItemDTO getOrderItemById(Long id);
    List<OrderItemDTO> getAllOrderItem();
    OrderItemDTO updateOrderItem(Long id, OrderItemDTO orderItemDTO);
    void deleteOrderItem(Long id);
}
