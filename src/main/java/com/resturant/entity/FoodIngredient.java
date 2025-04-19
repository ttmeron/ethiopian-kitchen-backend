package com.resturant.entity;

import lombok.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "food_ingredient")
public class FoodIngredient {

    @EmbeddedId
    private FoodIngredientId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("foodId")
    @JoinColumn(name = "food_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    private Food food;

    @ManyToOne
    @JoinColumn(name = "ingredient_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    private Ingredient ingredient;

    private BigDecimal extraCost; // If you want to store extra cost per ingredient in the relationship


    public FoodIngredient(Food food, Ingredient ingredient, BigDecimal extraCost) {
        this.food = food;
        this.ingredient = ingredient;
        this.extraCost = extraCost;

        this.id = new FoodIngredientId(food.getId(), ingredient.getId());
    }
}





