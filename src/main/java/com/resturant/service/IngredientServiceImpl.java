package com.resturant.service;


import com.resturant.dto.IngredientDTO;
import com.resturant.entity.Ingredient;
import com.resturant.mapper.IngredientMapper;
import com.resturant.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService {

    @Autowired
    IngredientMapper ingredientMapper;
    @Autowired
    IngredientRepository ingredientRepository;

    @Override
    public IngredientDTO createIngredient(IngredientDTO ingredientDTO) {

        Ingredient ingredient = ingredientMapper.toEntity(ingredientDTO);
        Ingredient savedIngredient = ingredientRepository.save(ingredient);

        return ingredientMapper.toDTO(savedIngredient);
    }

    @Override
    public IngredientDTO getIngredientById(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingredient not found with this Id: " + id));
        return ingredientMapper.toDTO(ingredient);
    }

    @Override
    public List<IngredientDTO> getAllIngredient() {
        return ingredientMapper.toDTOList(ingredientRepository.findAll());
    }

    @Override
    public IngredientDTO updateIngredient(Long id, IngredientDTO ingredientDTO) {
        Ingredient existingIngredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingredient not found with this Id: " + id));

        existingIngredient.setName(ingredientDTO.getName());
        existingIngredient.setExtraCost(ingredientDTO.getExtraCost());
        return ingredientMapper.toDTO(ingredientRepository.save(existingIngredient));
    }

    @Override
    public String deleteIngredient(Long id) {
        ingredientRepository.deleteById(id);
        return "You successful deleted Ingredient with id number: "+ id;

    }
}
