package com.resturant.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.resturant.dto.IngredientDTO;
import com.resturant.entity.Ingredient;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IngredientMapper {
    IngredientMapper INSTANCE = Mappers.getMapper(IngredientMapper.class);

    IngredientDTO toDTO(Ingredient ingredient);
    Ingredient toEntity(IngredientDTO ingredientDTO);
    List<IngredientDTO> toDTOList(List<Ingredient> ingredientList);
    List<Ingredient> toEntityList(List<IngredientDTO> dtos);
}
