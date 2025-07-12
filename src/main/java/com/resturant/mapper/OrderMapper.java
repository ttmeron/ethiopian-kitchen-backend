package com.resturant.mapper;

import com.resturant.dto.OrderDTO;
import com.resturant.entity.Order;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.Collections;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class, DeliveryMapper.class},imports = {Collections.class})
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);
    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "user.userName", target = "userName")
    @Mapping(source = "user.email", target = "email")
    @Mapping(target = "deliveryDTO", source = "delivery")
    @Mapping(target = "specialInstructions", source = "specialInstructions")
    @Mapping(source = "orderItems", target = "orderItems")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "paymentStatus", target = "paymentStatus")
    OrderDTO toDTO(Order order);


    @Mapping(target = "user", ignore = true)
    @Mapping(target = "delivery", ignore = true)
    @Mapping(target = "specialInstructions", source = "specialInstructions")
    Order toEntity(OrderDTO orderDTO);

}


