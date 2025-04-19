package com.resturant.service;

import com.resturant.dto.FoodDTO;
import com.resturant.dto.IngredientDTO;

import java.util.List;

public interface IngredientService {

    IngredientDTO createIngredient(IngredientDTO ingredientDTO);
    IngredientDTO getIngredientById(Long id);
    List<IngredientDTO> getAllIngredient();
    IngredientDTO updateIngredient(Long id, IngredientDTO ingredientDTO);
    String deleteIngredient(Long id);
}
