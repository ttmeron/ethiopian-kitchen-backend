package com.resturant.service;

import com.resturant.dto.OrderDTO;

import java.util.List;

public interface OrderService {

    OrderDTO placeOrder(OrderDTO orderDTO);
    OrderDTO getOrderById(Long id);
    List<OrderDTO> getAllOrder();
    OrderDTO updateOrder(Long id, OrderDTO orderDTO);
    void deleteOrder(Long id);
}
