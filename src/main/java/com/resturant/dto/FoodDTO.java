package com.resturant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for Food/Meal information")
public class FoodDTO {

    @Schema(
            description = "Unique identifier of the food item",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;
    @Schema(
            description = "Name of the food/dish",
            example = "Doro Wot",
            requiredMode = Schema.RequiredMode.REQUIRED
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
            description = "Price of the food item in Ethiopian Birr",
            example = "250.00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private BigDecimal price;
    @Schema(
            description = "Detailed description of the food item",
            example = "Spicy chicken stew with berbere sauce and hard-boiled eggs"
    )
    private String description;
    @Schema(
            description = "Path/URL to the food image",
            example = "/images/doro-wot.jpg"
    )
    private String imagePath;
    @Schema(
            description = "List of ingredient names (for display purposes)",
            example = "[\"Chicken\", \"Berbere spice\", \"Onions\"]"
    )
    private List<String> ingredientNames;
    @Schema(
            description = "Set of ingredient IDs (for processing)",
            example = "[1, 2, 3]"
    )
    private Set<Long> ingredientIds;
    @Schema(
            description = "Indicates whether the food has associated ingredient IDs",
            accessMode = Schema.AccessMode.READ_ONLY,
            hidden = true  // Hides from Swagger UI as it's a derived property
    )

    public boolean hasIngredientIds() {
        return ingredientIds != null && !ingredientIds.isEmpty();
    }

    public boolean hasIngredientNames() {
        return ingredientNames != null && !ingredientNames.isEmpty();
    }
}
