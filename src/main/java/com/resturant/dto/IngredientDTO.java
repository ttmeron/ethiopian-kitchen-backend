package com.resturant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ingredient information data transfer object")
public class IngredientDTO {
//

    @Schema(
            description = "Unique identifier of the ingredient",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;
//    private Long ingredientId;  // Must match JSON key
//    private double additionalCost;

    @Schema(
            description = "Name of the ingredient",
            example = "Berbere Spice",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 100
    )
    private String name;
    @Schema(
            description = "Additional cost when this ingredient is added as extra (in Ethiopian Birr)",
            example = "15.00",
            minimum = "0"
    )
    private BigDecimal extraCost;
}
