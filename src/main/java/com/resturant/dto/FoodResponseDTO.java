package com.resturant.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "Response payload containing food item details")
public class FoodResponseDTO {

    @Schema(
            description = "Unique identifier of the food item",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;
    @Schema(
            description = "Name of the Ethiopian dish",
            example = "Doro Wot"
    )
    private String name;

    @Schema(
            description = "Catagory of the Ethiopian dish",
            example = "Veggie",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 100
    )
    private String category;
    @Schema(
            description = "Price in Ethiopian Birr",
            example = "250.00",
            implementation = BigDecimal.class
    )
    private BigDecimal price;

    @Schema(
            description = "Detailed description of the dish",
            example = "Spicy chicken stew with berbere sauce and hard-boiled eggs"
    )
    private String description;
    @Schema(
            description = "URL or path to the food image",
            example = "/images/doro-wot.jpg",
            format = "uri-reference"
    )
    private String imagePath;
    @Schema(
            description = "List of ingredient names used in this dish",
            example = "[\"Chicken\", \"Berbere spice\", \"Onions\"]",
            type = "array"
    )
    private List<IngredientCostDTO> ingredients;


    @Schema(
            description = "Timestamp when the food item was created",
            example = "2023-07-15T14:30:00",
            format = "date-time",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;
//    private List<Long> ingredientIds;


}
