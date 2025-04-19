package com.resturant.mapper;

import com.resturant.dto.FoodDTO;
import com.resturant.dto.OrderItemDTO;
import com.resturant.entity.Food;
import com.resturant.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    OrderItemMapper INSTANCE = Mappers.getMapper(OrderItemMapper.class);

    @Mapping(source = "food.name", target = "foodName")
    @Mapping(source = "food.id", target = "foodId")
    @Mapping(target = "customIngredients", source = "orderItemIngredients")
    OrderItemDTO toDTO(OrderItem orderItem);

    @Mapping(target = "order", ignore = true)
    OrderItem toEntity(OrderItemDTO orderItemDTO);
    List<OrderItemDTO> toDTOList(List<OrderItem> orderItemList);

    List<OrderItem> toEntityList(List<OrderItemDTO> orderItemDTOList);

}
