package com.resturant.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class OrderItemIngredientDTO {

    @JsonIgnore
    private Long id;
    @NotNull(message = "Ingredient ID is required")
    private Long ingredientId;
    private Long orderItemId;
    private String ingredientName;
    @PositiveOrZero
    private BigDecimal extraCost;
    @Positive
    private int quantity;


    @JsonProperty("ingredientId")  // Ensures `ingredientId` appears in JSON
    public Long getIngredientId() {
        return ingredientId;
    }

    @JsonProperty("ingredientName")
    public String getIngredientName() {
        return ingredientName;
    }

}
