package com.resturant.repository;

import com.resturant.entity.FoodIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodIngredientRepository extends JpaRepository<FoodIngredient,Long> {

    List<FoodIngredient> findByFoodId(Long foodId);
    List<FoodIngredient> findByIngredientId(Long ingredientId);
}
