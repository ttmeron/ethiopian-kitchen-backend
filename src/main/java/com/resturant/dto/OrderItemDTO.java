package com.resturant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Individual item within an order")
public class OrderItemDTO {


    @Schema(
            description = "Unique identifier of the order item",
            example = "105",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long orderItemId;
    @Schema(
            description = "ID of the food item",
            example = "5",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Food ID is required")
    @JsonProperty("foodId")
    private Long foodId;


    @Schema(
            description = "Quantity ordered",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    @Positive
    private int quantity;

    @Schema(
            description = "Name of the Ethiopian food item",
            example = "Doro Wot",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String foodName;
    @Schema(
            description = "Price per unit in Ethiopian Birr",
            example = "250.00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private BigDecimal price;
    @ArraySchema(
            arraySchema = @Schema(
                    description = "Customizations to ingredients in this order item",
                    example = "[{\"ingredientId\": 1, \"modification\": \"EXTRA\"}]"
            ),
            schema = @Schema(implementation = OrderItemIngredientDTO.class)
    )
    private List<OrderItemIngredientDTO> customIngredients;

    @JsonProperty("foodId")          // Forces JSON to use `foodId` as "id"
    public Long getFoodId() {
        return foodId;
    }

}
