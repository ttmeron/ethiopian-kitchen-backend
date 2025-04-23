package com.resturant.controller;

import com.resturant.dto.IngredientDTO;
import com.resturant.exception.ErrorResponse;
import com.resturant.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ingredients")
@Tag(name = "Ingredient Management", description = "Operations for managing ingredients")
public class IngredientController {

    @Autowired
    IngredientService ingredientService;

    @PostMapping
    @Operation(
            summary = "Create a new ingredient",
            description = "Add a new ingredient to the system"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ingredient created successfully",
                    content = @Content(schema = @Schema(implementation = IngredientDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<IngredientDTO> createIngredient(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Ingredient details to create",
                    required = true,
                    content = @Content(schema = @Schema(implementation = IngredientDTO.class))
            )
            @RequestBody IngredientDTO ingredientDTO){
        IngredientDTO createdIngredient = ingredientService.createIngredient(ingredientDTO);
        return ResponseEntity.ok(createdIngredient);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get ingredient by ID",
            description = "Retrieve ingredient details by its unique identifier"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ingredient found",
                    content = @Content(schema = @Schema(implementation = IngredientDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ingredient not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<IngredientDTO> getIngredientById(
            @Parameter(
                    description = "ID of the ingredient to retrieve",
                    example = "1",
                    required = true
            )
            @PathVariable Long id){
        return ResponseEntity.ok(ingredientService.getIngredientById(id));
    }

    @GetMapping
    @Operation(
            summary = "Get all ingredients",
            description = "Retrieve a list of all available ingredients"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of all ingredients",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = IngredientDTO.class))))
    public ResponseEntity<List<IngredientDTO>> getAllIngredients(){
        return ResponseEntity.ok(ingredientService.getAllIngredient());
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an ingredient",
            description = "Modify an existing ingredient's details"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ingredient updated successfully",
                    content = @Content(schema = @Schema(implementation = IngredientDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ingredient not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<IngredientDTO> updateIngredient(
            @Parameter(
                    description = "ID of the ingredient to update",
                    example = "1",
                    required = true
            )
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated ingredient details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = IngredientDTO.class))
            )
            @RequestBody IngredientDTO ingredientDTO){
        return ResponseEntity.ok(ingredientService.updateIngredient(id,ingredientDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an ingredient",
            description = "Remove an ingredient from the system"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Ingredient deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ingredient not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> deleteIngredient(
            @Parameter(
                    description = "ID of the ingredient to delete",
                    example = "1",
                    required = true
            )@PathVariable Long id){
        ingredientService.deleteIngredient(id);
        return ResponseEntity.noContent().build();
    }
}
