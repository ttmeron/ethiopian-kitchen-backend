package com.resturant.mapper;

import com.resturant.dto.OrderItemDTO;
import com.resturant.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {OrderItemIngredientMapper.class})
public interface OrderItemMapper {

    OrderItemMapper INSTANCE = Mappers.getMapper(OrderItemMapper.class);

    @Mapping(source = "food.name", target = "foodName")
    @Mapping(source = "food.id", target = "foodId")
    @Mapping(source = "drink.name", target = "drinkName")
    @Mapping(source = "drink.id", target = "drinkId")
    @Mapping(target = "customIngredients", source = "orderItemIngredients")
    @Mapping(source = "size",target = "size")
    @Mapping(target = "itemType", expression = "java(getItemTypeString(orderItem))")
    @Mapping(source = "iceOption", target = "iceOption")
    @Mapping(source = "price", target = "price")

    OrderItemDTO toDTO(OrderItem orderItem);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "food", ignore = true)
    @Mapping(target = "drink", ignore = true)
    @Mapping(source = "drinkId", target = "drink.id")
    @Mapping(source = "foodId", target = "food.id")
    @Mapping(source = "size", target = "size")
    @Mapping(source = "iceOption", target = "iceOption")
    @Mapping(target = "itemType", expression = "java(getItemTypeEnum(orderItemDTO))")
    OrderItem toEntity(OrderItemDTO orderItemDTO);
    List<OrderItemDTO> toDTOList(List<OrderItem> orderItemList);

    List<OrderItem> toEntityList(List<OrderItemDTO> orderItemDTOList);

    default String getItemTypeString(OrderItem orderItem) {
        if (orderItem.getItemType() != null) {
            return orderItem.getItemType().getJsonValue();
        }
        return "FOOD";
    }

    default OrderItem.ItemType getItemType(OrderItem orderItem) {

        if (orderItem.getItemType() != null) {
            return orderItem.getItemType();
        }

        if (orderItem.getDrink() != null) {
            return OrderItem.ItemType.DRINK;
        }

        return OrderItem.ItemType.FOOD;
    }

    default OrderItem.ItemType mapItemType(String itemTypeString) {
        return itemTypeString != null ?
                OrderItem.ItemType.fromJsonValue(itemTypeString) : null;
    }
    default OrderItem.ItemType getItemTypeEnum(OrderItemDTO orderItemDTO) {
        if (orderItemDTO.getItemType() == null) {
            return OrderItem.ItemType.FOOD;
        }
        return OrderItem.ItemType.fromJsonValue(orderItemDTO.getItemType().toUpperCase());
    }

}
