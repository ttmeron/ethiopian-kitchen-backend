package com.resturant.service;

import com.resturant.dto.FoodRequestDTO;
import com.resturant.dto.FoodResponseDTO;
import com.resturant.dto.IngredientCostDTO;
import com.resturant.entity.Food;
import com.resturant.entity.FoodIngredient;
import com.resturant.entity.Ingredient;
import com.resturant.exception.ResourceNotFoundException;
import com.resturant.mapper.FoodMapper;
import com.resturant.repository.FoodIngredientRepository;
import com.resturant.repository.FoodRepository;
import com.resturant.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
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
    FoodIngredientRepository foodIngredientRepository;
    @Autowired
    FoodMapper foodMapper;


    @Override
    @Transactional
    public FoodResponseDTO createFood(FoodRequestDTO foodRequestDTO) {


        log.info("Received ingredients: {}", foodRequestDTO.getIngredients());

        Food food = new Food();
        food.setName(foodRequestDTO.getName());
        food.setPrice(foodRequestDTO.getPrice());
        food.setCategory(foodRequestDTO.getCategory());
        food.setDescription(foodRequestDTO.getDescription());
        food.setImagePath(foodRequestDTO.getImagePath());

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

            Food savedFood = foodRepository.save(food);
        log.info("Saved food ID: {}", savedFood.getId());
        log.info("Food's ingredients count: {}", savedFood.getFoodIngredients().size());
            return foodMapper.toResponseDTO(savedFood);

    }

    @Override
    public FoodResponseDTO getFoodById(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + id));


        food.getFoodIngredients().forEach(foodIngredient -> {
            Ingredient ingredient = foodIngredient.getIngredient();
        });

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

        foodMapper.updateFromRequest(foodRequestDTO, existingFood);

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
        food.clearIngredients();

        ingredientDTOs.forEach(ingredientDTO -> {
            Ingredient ingredient = ingredientRepository.findById(ingredientDTO.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Ingredient not found with id: " + ingredientDTO.getId()));
            food.addIngredient(ingredient, ingredientDTO.getExtraCost());
        });
    }

    private void updateIngredients(Food food, List<IngredientCostDTO> ingredientRequests) {

        Set<FoodIngredient> existingFoodIngredients = food.getFoodIngredients();

        Map<Long, FoodIngredient> existingMap = new HashMap<>();
        for (FoodIngredient fi : existingFoodIngredients) {
            existingMap.put(fi.getIngredient().getId(), fi);
        }

        Set<Long> newIngredientIds = new HashSet<>();

        for (IngredientCostDTO request : ingredientRequests) {
            newIngredientIds.add(request.getId());

            if (existingMap.containsKey(request.getId())) {

                FoodIngredient existing = existingMap.get(request.getId());
                existing.setExtraCost(request.getExtraCost());
            } else {

                Ingredient ingredient = ingredientRepository.findById(request.getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Ingredient not found with id: " + request.getId()));

                FoodIngredient foodIngredient = new FoodIngredient();
                foodIngredient.setFood(food);
                foodIngredient.setIngredient(ingredient);
                foodIngredient.setExtraCost(request.getExtraCost());

                food.getFoodIngredients().add(foodIngredient);
            }
        }

        Iterator<FoodIngredient> iterator = existingFoodIngredients.iterator();
        while (iterator.hasNext()) {
            FoodIngredient fi = iterator.next();
            if (!newIngredientIds.contains(fi.getIngredient().getId())) {
                iterator.remove();
                fi.setFood(null);
            }
        }
    }
}
