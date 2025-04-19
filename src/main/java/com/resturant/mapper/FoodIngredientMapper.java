package com.resturant.mapper;


import com.resturant.dto.FoodIngredientDTO;
import com.resturant.entity.Food;
import com.resturant.entity.FoodIngredient;
import com.resturant.entity.FoodIngredientId;
import com.resturant.entity.Ingredient;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FoodIngredientMapper {


    FoodIngredientMapper INSTANCE = Mappers.getMapper(FoodIngredientMapper.class);

    @Mapping(target = "foodId", source = "id.foodId")
    @Mapping(target = "ingredientId", source = "id.ingredientId")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    FoodIngredientDTO toDTO(FoodIngredient foodIngredient);

    @Mapping(target = "id", ignore = true)  // We'll handle this manually
    @Mapping(target = "food", ignore = true)
    @Mapping(target = "ingredient", ignore = true)
    FoodIngredient toEntity(FoodIngredientDTO foodIngredientDTO);


    default FoodIngredient toEntity(FoodIngredientDTO dto, Food food, Ingredient ingredient) {
        FoodIngredient entity = toEntity(dto);
        entity.setFood(food);
        entity.setIngredient(ingredient);
        entity.setId(new FoodIngredientId(food.getId(), ingredient.getId()));
        return entity;
    }
}
