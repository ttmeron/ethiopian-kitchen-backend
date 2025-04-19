package com.resturant.controller;

import com.resturant.dto.IngredientDTO;
import com.resturant.service.IngredientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")

public class IngredientController {

    @Autowired
    IngredientService ingredientService;

    @PostMapping
    public ResponseEntity<IngredientDTO> createIngredient(@RequestBody IngredientDTO ingredientDTO){
        IngredientDTO createdIngredient = ingredientService.createIngredient(ingredientDTO);
        return ResponseEntity.ok(createdIngredient);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngredientDTO> getIngredientById(@PathVariable Long id){
        return ResponseEntity.ok(ingredientService.getIngredientById(id));
    }

    @GetMapping
    public ResponseEntity<List<IngredientDTO>> getAllIngredients(){
        return ResponseEntity.ok(ingredientService.getAllIngredient());
    }

    @PutMapping("/{id}")
    public ResponseEntity<IngredientDTO> updateIngredient(@PathVariable Long id,@RequestBody IngredientDTO ingredientDTO){
        return ResponseEntity.ok(ingredientService.updateIngredient(id,ingredientDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long id){
        ingredientService.deleteIngredient(id);
        return ResponseEntity.noContent().build();
    }
}
