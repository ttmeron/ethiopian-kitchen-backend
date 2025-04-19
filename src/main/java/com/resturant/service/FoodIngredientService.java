package com.resturant.service;

import com.resturant.dto.FoodIngredientDTO;

import java.util.List;

public interface FoodIngredientService {

    public FoodIngredientDTO createFoodIngredient(FoodIngredientDTO foodIngredientDTO);
    public List<FoodIngredientDTO> getAllFoodIngredients();
    public FoodIngredientDTO getFoodIngredientById(Long id);
    public FoodIngredientDTO updateFoodIngredient(Long id, FoodIngredientDTO foodIngredientDTO);
    public void deleteFoodIngredient(Long id);
}
