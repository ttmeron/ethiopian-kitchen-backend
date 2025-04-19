package com.resturant.controller;


import com.resturant.dto.FoodIngredientDTO;
import com.resturant.service.FoodIngredientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food-ingredients")
public class FoodIngredientController {

    @Autowired
    private FoodIngredientService foodIngredientService;

    @PostMapping
    public ResponseEntity<FoodIngredientDTO> createFoodIngredient(@RequestBody FoodIngredientDTO foodIngredientDTO) {
        FoodIngredientDTO createdFoodIngredient = foodIngredientService.createFoodIngredient(foodIngredientDTO);
        return new ResponseEntity<>(createdFoodIngredient, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FoodIngredientDTO>> getAllFoodIngredients() {
        List<FoodIngredientDTO> foodIngredients = foodIngredientService.getAllFoodIngredients();
        return new ResponseEntity<>(foodIngredients, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodIngredientDTO> getFoodIngredientById(@PathVariable Long id) {
        FoodIngredientDTO foodIngredient = foodIngredientService.getFoodIngredientById(id);
        return new ResponseEntity<>(foodIngredient, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodIngredientDTO> updateFoodIngredient(
            @PathVariable Long id, @RequestBody FoodIngredientDTO foodIngredientDTO) {
        FoodIngredientDTO updatedFoodIngredient = foodIngredientService.updateFoodIngredient(id, foodIngredientDTO);
        return new ResponseEntity<>(updatedFoodIngredient, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFoodIngredient(@PathVariable Long id) {
        foodIngredientService.deleteFoodIngredient(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
