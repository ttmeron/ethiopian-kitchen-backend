package com.resturant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Request payload for creating or updating food items")
public class FoodRequestDTO {
    @Schema(
            description = "Name of the Ethiopian dish",
            example = "Doro Wot",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 100
    )
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    private String name;
    @Schema(
            description = "Catagory of the Ethiopian dish",
            example = "Veggie",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 100
    )
    private String category;
    @Schema(
            description = "Path/URL to the food image (automatically set when uploading)",
            example = "/uploads/doro-wot.jpg",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String imagePath;

//    private String imageName;  // Add this

    @Schema(
            description = "Price of the dish in Ethiopian Birr",
            example = "250.00",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0.01"
    )
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @Schema(
            description = "Description of the dish",
            example = "Spicy chicken stew with berbere sauce and hard-boiled eggs",
            maxLength = 500
    )
    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @Schema(
            description = "List of ingredients with their quantities and costs",
            implementation = IngredientCostDTO.class
    )
    private List<IngredientCostDTO> ingredients;
}
