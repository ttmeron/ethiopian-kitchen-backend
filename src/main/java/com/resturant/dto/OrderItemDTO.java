package com.resturant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

public class OrderItemDTO {

    private Long orderItemId;

    @NotNull(message = "Food ID is required")
    @JsonProperty("foodId")
    private Long foodId;
    private String foodName;

    @NotNull
    @Positive
    private int quantity;
    private BigDecimal price;
    private List<OrderItemIngredientDTO> customIngredients;

    @JsonProperty("foodId")          // Forces JSON to use `foodId` as "id"
    public Long getFoodId() {
        return foodId;
    }

}
