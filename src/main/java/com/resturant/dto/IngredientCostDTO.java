package com.resturant.dto;

import lombok.*;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IngredientCostDTO {
    @NotNull
    private Long id;
    @PositiveOrZero
    private BigDecimal extraCost;
}
