package com.resturant.mapper;

import com.resturant.dto.FoodDTO;
import com.resturant.dto.FoodRequestDTO;
import com.resturant.dto.FoodResponseDTO;
import com.resturant.entity.Food;
import com.resturant.entity.FoodIngredient;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring")
public interface FoodMapper {
    FoodMapper INSTANCE = Mappers.getMapper(FoodMapper.class);

    // Basic mappings
    FoodDTO toDTO(Food food);
    Food toEntity(FoodDTO foodDTO);
    Set<FoodDTO> toDtoSet(Set<Food> foods);
    List<FoodDTO> toDTOList(List<Food> foodList);
    List<Food> toEntityList(List<FoodDTO> foodDTOList);

    // Request mapping (ignore ingredients during initial conversion)
    @Mapping(target = "foodIngredients", ignore = true)
    @Mapping(source = "imagePath", target = "imagePath")
    Food toEntityFromRequest(FoodRequestDTO requestDTO);


    // Response mapping with explicit ingredient handling
    @Mapping(target = "ingredientNames", expression = "java(mapIngredientNames(food.getFoodIngredients()))")
    FoodResponseDTO toResponseDTO(Food food);

    // Update mapping
    @Mapping(target = "foodIngredients", ignore = true)
    void updateFromRequest(FoodRequestDTO requestDTO, @MappingTarget Food food);

    // Ingredient name mapping
    default List<String> mapIngredientNamesFromFood(Food food) {
        if (food.getFoodIngredients() == null || food.getFoodIngredients().isEmpty()) {
            return Collections.emptyList();
        }
        return food.getFoodIngredients().stream()
                .map(fi -> fi.getIngredient().getName())
                .collect(Collectors.toList());
    }

    // Ingredient ID mapping
    default List<Long> mapIngredientIdsFromFood(Food food) {
        if (food.getFoodIngredients() == null || food.getFoodIngredients().isEmpty()) {
            return Collections.emptyList();
        }
        return food.getFoodIngredients().stream()
                .map(fi -> fi.getIngredient().getId())
                .collect(Collectors.toList());
    }

    // AfterMapping callback for additional processing
    @AfterMapping
    default void enhanceResponseDTO(Food food, @MappingTarget FoodResponseDTO responseDTO) {
        // Set createdAt if not already set
        if (responseDTO.getCreatedAt() == null) {
            responseDTO.setCreatedAt(LocalDateTime.now());
        }

        // Ensure collections are never null
        if (responseDTO.getIngredientNames() == null) {
            responseDTO.setIngredientNames(Collections.emptyList());
        }
    }

    default List<String> mapIngredientNames(Set<FoodIngredient> foodIngredients) {
        if (foodIngredients == null) return Collections.emptyList();
        return foodIngredients.stream()
                .map(fi -> fi.getIngredient().getName())
                .collect(Collectors.toList());
    }
}
