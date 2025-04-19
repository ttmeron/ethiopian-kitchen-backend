package com.resturant.service;

import com.resturant.dto.FoodIngredientDTO;
import com.resturant.entity.FoodIngredient;
import com.resturant.exception.ResourceNotFoundException;
import com.resturant.mapper.FoodIngredientMapper;
import com.resturant.repository.FoodIngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FoodIngredientServiceImpl implements FoodIngredientService {

    @Autowired
    private FoodIngredientRepository foodIngredientRepository;

    @Autowired
    private FoodIngredientMapper foodIngredientMapper;


    @Override
    public FoodIngredientDTO createFoodIngredient(FoodIngredientDTO foodIngredientDTO) {
        FoodIngredient foodIngredient = foodIngredientMapper.toEntity(foodIngredientDTO);
        FoodIngredient savedFoodIngredient = foodIngredientRepository.save(foodIngredient);
        return foodIngredientMapper.toDTO(savedFoodIngredient);
    }

    @Override
    public List<FoodIngredientDTO> getAllFoodIngredients() {
        List<FoodIngredient> foodIngredients = foodIngredientRepository.findAll();
        return foodIngredients.stream()
                .map(foodIngredientMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FoodIngredientDTO getFoodIngredientById(Long id) {
        Optional<FoodIngredient> foodIngredient = foodIngredientRepository.findById(id);
        if (foodIngredient.isPresent()) {
            return foodIngredientMapper.toDTO(foodIngredient.get());
        }
        throw new ResourceNotFoundException("FoodIngredient not found with id " + id);
    }


    @Override
    public FoodIngredientDTO updateFoodIngredient(Long id, FoodIngredientDTO foodIngredientDTO) {
        if (!foodIngredientRepository.existsById(id)) {
            throw new ResourceNotFoundException("FoodIngredient not found with id " + id);
        }
        foodIngredientDTO.setFoodId(id);
        FoodIngredient foodIngredient = foodIngredientMapper.toEntity(foodIngredientDTO);
        FoodIngredient updatedFoodIngredient = foodIngredientRepository.save(foodIngredient);
        return foodIngredientMapper.toDTO(updatedFoodIngredient);
    }

    @Override
    public void deleteFoodIngredient(Long id) {
        if (!foodIngredientRepository.existsById(id)) {
            throw new ResourceNotFoundException("FoodIngredient not found with id " + id);
        }
        foodIngredientRepository.deleteById(id);
    }
}
