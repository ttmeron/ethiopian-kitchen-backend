package com.resturant.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FoodIngredientDTO {


    private Long foodId;
    private Long ingredientId;

    private String ingredientName;

    private BigDecimal extraCost;
}
