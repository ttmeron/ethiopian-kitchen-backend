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
    @Mapping(source = "user.userName", target = "userName")
    @Mapping(source = "user.email", target = "email")
    @Mapping(target = "deliveryDTO", source = "delivery")
    OrderDTO toDTO(Order order);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "delivery", ignore = true) // Delivery handled separately
    Order toEntity(OrderDTO orderDTO);

}


