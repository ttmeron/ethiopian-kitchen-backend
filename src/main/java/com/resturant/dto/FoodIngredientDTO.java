package com.resturant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Food-Ingredient relationship information")
public class FoodIngredientDTO {


    @Schema(
            description = "ID of the food item",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long foodId;
    @Schema(
            description = "ID of the ingredient",
            example = "5",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long ingredientId;

    @Schema(
            description = "Name of the ingredient (read-only)",
            example = "Berbere Spice",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String ingredientName;

    @Schema(
            description = "Additional cost for this ingredient in the dish (Ethiopian Birr)",
            example = "15.00",
            minimum = "0"
    )
    private BigDecimal extraCost;
}
