package com.resturant.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FoodDTO {

    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private String imagePath;
    private List<String> ingredientNames;
    private Set<Long> ingredientIds;

    public boolean hasIngredientIds() {
        return ingredientIds != null && !ingredientIds.isEmpty();
    }

    public boolean hasIngredientNames() {
        return ingredientNames != null && !ingredientNames.isEmpty();
    }
}
