package com.resturant.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Customized ingredient details for an order item")
public class OrderItemIngredientDTO {

    @Schema(
            description = "Internal ID (not exposed in API responses)",
            accessMode = Schema.AccessMode.READ_ONLY,
            hidden = true
    )
    @JsonIgnore
    private Long id;
    @Schema(
            description = "ID of the ingredient being customized",
            example = "5",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Ingredient ID is required")
    private Long ingredientId;
    @Schema(
            description = "Associated order item ID",
            example = "105",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long orderItemId;
    @Schema(
            description = "Name of the ingredient (read-only)",
            example = "Berbere Spice",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String ingredientName;
    @Schema(
            description = "Additional cost for this customization in Ethiopian Birr",
            example = "15.00",
            minimum = "0"
    )
    @PositiveOrZero
    private BigDecimal extraCost;
    @Schema(
            description = "Quantity of the modification (e.g., 2 for 'double')",
            example = "1",
            minimum = "1"
    )
    @Positive
    private int quantity;


    @Schema(
            description = "Ingredient ID (serialized field)",
            accessMode = Schema.AccessMode.READ_ONLY,
            hidden = true
    )
    @JsonProperty("ingredientId")  // Ensures `ingredientId` appears in JSON
    public Long getIngredientId() {
        return ingredientId;
    }

    @Schema(
            description = "Ingredient name (serialized field)",
            accessMode = Schema.AccessMode.READ_ONLY,
            hidden = true
    )
    @JsonProperty("ingredientName")
    public String getIngredientName() {
        return ingredientName;
    }

}
