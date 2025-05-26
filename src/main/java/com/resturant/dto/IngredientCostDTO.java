package com.resturant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ingredient information with cost details")
public class IngredientCostDTO {
    @Schema(
            description = "ID of the ingredient",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    private Long id;
    @Schema(
            description = "Additional ingredient in this dish",
            example = "Enjera"
    )
    private String name;
    @PositiveOrZero
    @Schema(
            description = "Additional cost for this ingredient in this dish",
            example = "1.00"
    )
    private BigDecimal extraCost;
}
