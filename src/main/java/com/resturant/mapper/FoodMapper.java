package com.resturant.mapper;

import com.resturant.dto.FoodDTO;
import com.resturant.dto.FoodRequestDTO;
import com.resturant.dto.FoodResponseDTO;
import com.resturant.dto.IngredientCostDTO;
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

    FoodDTO toDTO(Food food);
    Food toEntity(FoodDTO foodDTO);
    Set<FoodDTO> toDtoSet(Set<Food> foods);
    List<FoodDTO> toDTOList(List<Food> foodList);
    List<Food> toEntityList(List<FoodDTO> foodDTOList);

    @Mapping(target = "ingredients", expression = "java(toIngredientCosts(food.getFoodIngredients()))")
    @Mapping(source = "category", target = "category")
    FoodResponseDTO toResponseDTO(Food food);

    @Mapping(target = "foodIngredients", ignore = true)
    void updateFromRequest(FoodRequestDTO requestDTO, @MappingTarget Food food);


    default List<IngredientCostDTO> toIngredientCosts(Set<FoodIngredient> foodIngredients) {
        if (foodIngredients == null || foodIngredients.isEmpty()) {
            return Collections.emptyList();
        }
        return foodIngredients.stream()
                .map(this::toIngredientCostDTO)
                .collect(Collectors.toList());
    }

    default IngredientCostDTO toIngredientCostDTO(FoodIngredient fi) {
        if (fi == null || fi.getIngredient() == null) return null;

        IngredientCostDTO dto = new IngredientCostDTO();
        dto.setId(fi.getIngredient().getId());
        dto.setName(fi.getIngredient().getName());
        dto.setExtraCost(fi.getExtraCost());
        return dto;
    }

    @AfterMapping
    default void enhanceResponseDTO(Food food, @MappingTarget FoodResponseDTO responseDTO) {
        if (responseDTO.getCreatedAt() == null) {
            responseDTO.setCreatedAt(LocalDateTime.now());
        }
        if (responseDTO.getIngredients() == null) {
            responseDTO.setIngredients(Collections.emptyList());
        }
    }
}
