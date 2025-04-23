package com.resturant.controller;


import com.resturant.dto.FoodIngredientDTO;
import com.resturant.exception.ErrorResponse;
import com.resturant.service.FoodIngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/food-ingredients")
@Tag(name = "Food Ingredient Management",
        description = "Operations for managing relationships between foods and ingredients")
public class FoodIngredientController {

    @Autowired
    private FoodIngredientService foodIngredientService;

    @PostMapping
    @Operation(
            summary = "Create a new food-ingredient relationship",
            description = "Add an ingredient to a food item with optional extra cost"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Food-Ingredient relationship created successfully",
                    content = @Content(schema = @Schema(implementation = FoodIngredientDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                    })
    public ResponseEntity<FoodIngredientDTO> createFoodIngredient(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Food-Ingredient details to create",
                    required = true,
                    content = @Content(schema = @Schema(implementation = FoodIngredientDTO.class))
            )
            @RequestBody FoodIngredientDTO foodIngredientDTO) {
        FoodIngredientDTO createdFoodIngredient = foodIngredientService.createFoodIngredient(foodIngredientDTO);
        return new ResponseEntity<>(createdFoodIngredient, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "Get all food-ingredient relationships",
            description = "Retrieve a list of all food-ingredient combinations"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of all food-ingredient relationships",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = FoodIngredientDTO.class))))
    public ResponseEntity<List<FoodIngredientDTO>> getAllFoodIngredients() {
        List<FoodIngredientDTO> foodIngredients = foodIngredientService.getAllFoodIngredients();
        return new ResponseEntity<>(foodIngredients, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get food-ingredient by ID",
            description = "Retrieve specific food-ingredient relationship details"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Food-Ingredient relationship found",
                    content = @Content(schema = @Schema(implementation = FoodIngredientDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Food-Ingredient relationship not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FoodIngredientDTO> getFoodIngredientById(
            @Parameter(
                    description = "ID of the food-ingredient relationship",
                    example = "1",
                    required = true
            )
            @PathVariable Long id) {
        FoodIngredientDTO foodIngredient = foodIngredientService.getFoodIngredientById(id);
        return new ResponseEntity<>(foodIngredient, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update food-ingredient relationship",
            description = "Modify an existing food-ingredient relationship"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Food-Ingredient relationship updated successfully",
                    content = @Content(schema = @Schema(implementation = FoodIngredientDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Food-Ingredient relationship not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FoodIngredientDTO> updateFoodIngredient(
            @Parameter(
                    description = "ID of the food-ingredient relationship to update",
                    example = "1",
                    required = true
            )
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated food-ingredient details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = FoodIngredientDTO.class))
            )
            @RequestBody FoodIngredientDTO foodIngredientDTO) {
        FoodIngredientDTO updatedFoodIngredient = foodIngredientService.updateFoodIngredient(id, foodIngredientDTO);
        return new ResponseEntity<>(updatedFoodIngredient, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete food-ingredient relationship",
            description = "Remove an ingredient from a food item"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Food-Ingredient relationship deleted successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Food-Ingredient relationship not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteFoodIngredient(
            @Parameter(
                    description = "ID of the food-ingredient relationship to delete",
                    example = "1",
                    required = true
            )
            @PathVariable Long id) {
        foodIngredientService.deleteFoodIngredient(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
