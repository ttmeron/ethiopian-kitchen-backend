package com.resturant.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ingredient")
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "extra_cost", precision = 10, scale = 2)
    private BigDecimal extraCost;


    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL)
    private List<OrderItemIngredient> orderItemIngredients;

    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FoodIngredient> foodIngredients = new HashSet<>();

    public void addOrderItemIngredient(OrderItemIngredient orderItemIngredient) {
        this.orderItemIngredients.add(orderItemIngredient);
        orderItemIngredient.setIngredient(this);
    }

    public void removeOrderItemIngredient(OrderItemIngredient orderItemIngredient) {
        this.orderItemIngredients.remove(orderItemIngredient);
        orderItemIngredient.setIngredient(null);
    }

    public void addFoodIngredient(FoodIngredient foodIngredient) {
        this.foodIngredients.add(foodIngredient);
    }

    public Ingredient(Long id) {
        this.id = id;
    }
    // equals() and hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ingredient)) return false;
        return id != null && id.equals(((Ingredient) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
