package com.resturant.mapper;

import com.resturant.dto.OrderItemIngredientDTO;
import com.resturant.entity.Ingredient;
import com.resturant.entity.OrderItemIngredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemIngredientMapper {
    OrderItemIngredientMapper INSTANCE = Mappers.getMapper(OrderItemIngredientMapper.class);

    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    OrderItemIngredientDTO toDTO(OrderItemIngredient orderItemIngredient);


    @Mapping(target = "orderItem", ignore = true)
    @Mapping(target = "ingredient", source = "ingredientId")
    OrderItemIngredient toEntity(OrderItemIngredientDTO orderItemIngredientDTO);

    default Ingredient map(Long ingredientId) {
        if (ingredientId == null) {
            return null;
        }
        Ingredient ingredient = new Ingredient();
        ingredient.setId(ingredientId);
        return ingredient;
    }


    List<OrderItemIngredientDTO> toDTOList(List<OrderItemIngredient> list);
    List<OrderItemIngredient> toEntityList(List<OrderItemIngredientDTO> list);

}
