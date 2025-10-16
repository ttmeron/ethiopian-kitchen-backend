package com.resturant.mapper;

import com.resturant.dto.OrderDTO;
import com.resturant.dto.response.GuestOrderResponseDTO;
import com.resturant.entity.Order;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class, DeliveryMapper.class},imports = {Collections.class})
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);
    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "createdAt", target = "placedTime")
    @Mapping(expression = "java(order.getUser() != null ? order.getUser().getUserName() : order.getGuestName())", target = "userName")
    @Mapping(expression = "java(order.getUser() != null ? order.getUser().getEmail() : order.getGuestEmail())", target = "email")
    @Mapping(target = "deliveryDTO", source = "delivery")
    @Mapping(source = "orderItems", target = "orderItems")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "paymentStatus", target = "paymentStatus")
    @Mapping(source = "trackingToken", target = "trackingToken")
    @Mapping(source = "specialInstructions", target = "specialInstructions")
    @Mapping(expression = "java(order.getIsGuest() != null ? order.getIsGuest() : false)", target = "guest")

    @Mapping(source = "totalPrice", target = "totalPrice")
    OrderDTO toDTO(Order order);

    List<OrderDTO> toDTOList(List<Order> orders);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "delivery", ignore = true)
    @Mapping(target = "specialInstructions", source = "specialInstructions")
    Order toEntity(OrderDTO orderDTO);

    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "createdAt", target = "placedTime")
    @Mapping(source = "orderItems", target = "orderItems")
    @Mapping(source = "delivery", target = "deliveryDTO")
    @Mapping(source = "totalPrice", target = "totalPrice")
    @Mapping(expression = "java(order.getIsGuest() != null ? order.getIsGuest() : false)", target = "guest")
    GuestOrderResponseDTO toGuestResponseDTO(Order order);

    List<GuestOrderResponseDTO> toGuestResponseDTOList(List<Order> orders);


}


