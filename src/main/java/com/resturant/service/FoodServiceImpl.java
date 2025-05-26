package com.resturant.service;

import com.resturant.dto.FoodDTO;
import com.resturant.dto.FoodRequestDTO;
import com.resturant.dto.FoodResponseDTO;
import com.resturant.dto.IngredientCostDTO;
import com.resturant.entity.Food;
import com.resturant.entity.FoodIngredient;
import com.resturant.entity.Ingredient;
import com.resturant.exception.ResourceNotFoundException;
import com.resturant.mapper.FoodMapper;
import com.resturant.repository.FoodRepository;
import com.resturant.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class FoodServiceImpl implements FoodService{

    @Autowired
    FoodRepository foodRepository;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    FoodMapper foodMapper;


    @Override
    @Transactional
    public FoodResponseDTO createFood(FoodRequestDTO foodRequestDTO) {


        log.info("Received ingredients: {}", foodRequestDTO.getIngredients());
        // 1. First create and save the food entity alone
        Food food = new Food();
        food.setName(foodRequestDTO.getName());
        food.setPrice(foodRequestDTO.getPrice());
        food.setCategory(foodRequestDTO.getCategory());
        food.setDescription(foodRequestDTO.getDescription());
        food.setImagePath(foodRequestDTO.getImagePath());

        // 2. Process ingredients only after food is persisted
        if (foodRequestDTO.getIngredients() != null && !foodRequestDTO.getIngredients().isEmpty()) {

            log.info("Processing {} ingredients", foodRequestDTO.getIngredients().size());

            for (IngredientCostDTO ingredientDTO : foodRequestDTO.getIngredients()) {

                log.info("Adding ingredient ID: {}", ingredientDTO.getId());
                Ingredient ingredient = ingredientRepository.findById(ingredientDTO.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));


                food.addIngredient(ingredient,
                        ingredientDTO.getExtraCost() != null ?
                                ingredientDTO.getExtraCost() : BigDecimal.ZERO);
            }
        }

            // Set the collection and save again
            Food savedFood = foodRepository.save(food);
        log.info("Saved food ID: {}", savedFood.getId());
        log.info("Food's ingredients count: {}", savedFood.getFoodIngredients().size());
            return foodMapper.toResponseDTO(savedFood);

    }



    @Override
    public FoodResponseDTO getFoodById(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + id));
        return foodMapper.toResponseDTO(food);
    }

    @Override
    public List<FoodResponseDTO> getAllFood() {
        return foodRepository.findAll().stream()
                .map(foodMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FoodResponseDTO updateFood(Long id, FoodRequestDTO foodRequestDTO) {
        validateFoodRequest(foodRequestDTO);

        Food existingFood = foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + id));

        // Update basic fields
        foodMapper.updateFromRequest(foodRequestDTO, existingFood);

        // Handle ingredients update
        if (foodRequestDTO.getIngredients() != null) {
            updateIngredients(existingFood, foodRequestDTO.getIngredients());
        }

        Food updatedFood = foodRepository.save(existingFood);
        return foodMapper.toResponseDTO(updatedFood);
    }

    @Override
    public void deleteFood(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + id));
        foodRepository.delete(food);
    }

    // Helper methods
    private void validateFoodRequest(FoodRequestDTO foodRequestDTO) {
        if (foodRequestDTO == null) {
            throw new IllegalArgumentException("Food request cannot be null");
        }
        if (foodRequestDTO.getName() == null || foodRequestDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Food name cannot be empty");
        }
        if (foodRequestDTO.getPrice() == null || foodRequestDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Food price must be positive");
        }
    }

    private void processIngredients(Food food, List<IngredientCostDTO> ingredientDTOs) {
        food.clearIngredients(); // Safely clears existing ingredients

        ingredientDTOs.forEach(ingredientDTO -> {
            Ingredient ingredient = ingredientRepository.findById(ingredientDTO.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Ingredient not found with id: " + ingredientDTO.getId()));
            food.addIngredient(ingredient, ingredientDTO.getExtraCost());
        });
    }

    private void updateIngredients(Food food, List<IngredientCostDTO> ingredientDTOs) {
        food.clearIngredients(); // Safely clears existing ingredients

        if (!ingredientDTOs.isEmpty()) {
            processIngredients(food, ingredientDTOs);
        }
    }
}
