package com.resturant.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resturant.dto.FoodRequestDTO;
import com.resturant.dto.FoodResponseDTO;
import com.resturant.exception.ErrorResponse;
import com.resturant.service.FileManagementService;
import com.resturant.service.FoodServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/foods")
@Tag(name = "Food Management", description = "Operations related to Ethiopian food items")

public class FoodController {

    @Autowired
    FoodServiceImpl foodService;
    @Autowired
    FileManagementService fileManagementService;


    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping(value = "/admin",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create a new food item with optional image",
            description = "Upload food details along with an optional image file"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Food created successfully",
                    content = @Content(schema = @Schema(implementation = FoodResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or file type",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> createFood(
            @Parameter(
                    description = "Food data in JSON format",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FoodRequestDTO.class))
            )
            @RequestPart("foodRequest") String rawJson,
            @Parameter(
                    description = "Image file for the food item",
                    content = @Content(mediaType = "image/*")
            )
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile){

        try {
            ObjectMapper mapper = new ObjectMapper();
            FoodRequestDTO foodRequestDTO = mapper.readValue(rawJson, FoodRequestDTO.class);


            System.out.println("Food request: " + foodRequestDTO);
            if (imageFile != null && !imageFile.isEmpty()) {
                System.out.println("Image file content type: " + imageFile.getContentType());

                // Validate file type
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "Only image files are allowed");
                if (!imageFile.getContentType().startsWith("image/")) {
                    return ResponseEntity.badRequest()
                            .body(errorMap);
                }

                String imagePath = fileManagementService.saveFile(imageFile);
                foodRequestDTO.setImagePath(imagePath);
            }

            FoodResponseDTO response = foodService.createFood(foodRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IOException e) {
            Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "File storage failed");
                errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Food creation failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }


    @GetMapping("/{id}")
    @Operation(summary = "Get food item by ID")
    @ApiResponse(
            responseCode = "200",
            description = "Food item found",
            content = @Content(schema = @Schema(implementation = FoodResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Food item not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<FoodResponseDTO> getFoodById(
            @Parameter(description = "ID of the food item", example = "1")
            @PathVariable Long id){
        FoodResponseDTO food = foodService.getFoodById(id);
        return ResponseEntity.ok(food);
    }

    @GetMapping
    @Operation(summary = "Get all food items")
    @ApiResponse(
            responseCode = "200",
            description = "List of all food items",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = FoodResponseDTO.class))))

    public ResponseEntity<List<FoodResponseDTO>> getAllFood(){
        return ResponseEntity.ok(foodService.getAllFood());
    }

    @PutMapping(value = "/admin/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateFood(
            @PathVariable Long id,
            @RequestPart("foodRequest") String rawJson,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {

        try {

            // Check if rawJson is provided (it should be required)
            if (rawJson == null || rawJson.trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "foodRequest is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            ObjectMapper mapper = new ObjectMapper();
            FoodRequestDTO foodRequestDTO = mapper.readValue(rawJson, FoodRequestDTO.class);

            // Handle image if provided
            if (imageFile != null && !imageFile.isEmpty()) {

                if (!imageFile.getContentType().startsWith("image/")) {
                    Map<String, String> errorMap = new HashMap<>();
                    errorMap.put("error", "Only image files are allowed");
                    return ResponseEntity.badRequest().body(errorMap);
                }

                String imagePath = fileManagementService.saveFile(imageFile);
                foodRequestDTO.setImagePath(imagePath);
            }

            FoodResponseDTO response = foodService.updateFood(id, foodRequestDTO);
            return ResponseEntity.ok(response);

        } catch (JsonProcessingException e) {
            System.err.println("JSON parsing error: " + e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid JSON format");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            System.err.println("Update error: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Food update failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    @DeleteMapping("/admin/{id}")
    @Operation(summary = "Delete a food item")
    @ApiResponse(
            responseCode = "204",
            description = "Food deleted successfully"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Food item not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<Void> deleteFood(
            @Parameter(description = "ID of the food to delete", example = "1")
            @PathVariable Long id){
        foodService.deleteFood(id);
        return ResponseEntity.noContent().build();
    }

}
