package com.resturant.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "foods")
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    private String description;
    @Column(name = "category")
    private String category;
    private String imagePath;

    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FoodIngredient> foodIngredients = new HashSet<>();


    public void addIngredient(Ingredient ingredient, BigDecimal extraCost) {
        FoodIngredient foodIngredient = new FoodIngredient(this, ingredient, extraCost);
        this.foodIngredients.add(foodIngredient);
        ingredient.getFoodIngredients().add(foodIngredient); // Maintain bidirectional relationship
    }
    public void clearIngredients() {
        foodIngredients.clear();
    }
    // Helper method to safely manage ingredients
    public void updateIngredients(Set<FoodIngredient> newIngredients) {
        // Clear existing ingredients safely
        this.foodIngredients.clear();


        // Add new ingredients while maintaining bidirectional relationship
        newIngredients.forEach(fi -> {
            fi.setFood(this);
            this.foodIngredients.add(fi);
        });
    }




    public void removeIngredient(Ingredient ingredient) {
        this.foodIngredients.removeIf(foodIngredient -> foodIngredient.getIngredient().equals(ingredient));
        ingredient.getFoodIngredients().removeIf(foodIngredient -> foodIngredient.getFood().equals(this));
    }


}
