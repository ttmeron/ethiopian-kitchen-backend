package com.resturant.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FoodIngredientId implements Serializable {

    @Column(name = "food_id")
    private Long foodId;

    @Column(name = "ingredient_id")
    private Long ingredientId;
}
