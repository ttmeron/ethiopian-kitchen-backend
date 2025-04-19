package com.resturant.service;

import com.resturant.dto.FoodDTO;
import com.resturant.dto.FoodRequestDTO;
import com.resturant.dto.FoodResponseDTO;

import java.util.List;

public interface FoodService {

    FoodResponseDTO createFood(FoodRequestDTO foodRequestDTO);
    FoodResponseDTO getFoodById(Long id);
    List<FoodResponseDTO> getAllFood();
    FoodResponseDTO updateFood(Long id, FoodRequestDTO foodRequestDTO);
    void deleteFood(Long id);
}
