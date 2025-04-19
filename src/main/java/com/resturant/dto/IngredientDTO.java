package com.resturant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDTO {
//
    private Long id;
//    private Long ingredientId;  // Must match JSON key
//    private double additionalCost;
    private String name;
    private BigDecimal extraCost;
}
