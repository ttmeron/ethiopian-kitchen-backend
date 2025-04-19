package com.resturant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resturant.dto.FoodDTO;
import com.resturant.dto.FoodRequestDTO;
import com.resturant.dto.FoodResponseDTO;
import com.resturant.service.FileManagementService;
import com.resturant.service.FoodServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/foods")
public class FoodController {

    @Autowired
    FoodServiceImpl foodService;
    @Autowired
    FileManagementService fileManagementService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createFood(
//            @RequestPart("foodRequest") @Valid  FoodRequestDTO foodRequestDTO,
            @RequestPart("foodRequest") String rawJson,

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
    public ResponseEntity<FoodResponseDTO> getFoodById(@PathVariable Long id){
        FoodResponseDTO food = foodService.getFoodById(id);
        return ResponseEntity.ok(food);
    }

    @GetMapping
    public ResponseEntity<List<FoodResponseDTO>> getAllFood(){
        return ResponseEntity.ok(foodService.getAllFood());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodResponseDTO> updateFood(
            @PathVariable Long id,
            @Valid @RequestBody FoodRequestDTO foodRequestDTO){
       return ResponseEntity.ok(foodService.updateFood(id,foodRequestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(@PathVariable Long id){
        foodService.deleteFood(id);
        return ResponseEntity.noContent().build();
    }



}
