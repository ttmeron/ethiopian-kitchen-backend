package com.resturant.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Table(name = "\"order_item\"")
public class OrderItem {
    public enum ItemType {
        FOOD("FOOD"),
        DRINK("DRINK");

        private String jsonValue;

        ItemType(String jsonValue) {
            this.jsonValue = jsonValue;
        }

        @JsonValue
        public String getJsonValue() {
            return jsonValue;
        }

        @JsonCreator
        public static ItemType fromJsonValue(String value) {
            for (ItemType type : values()) {
                if (type.jsonValue.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown item type: " + value);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;
    private String size;
    private String iceOption;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id")

    private Food food;

    private int quantity;
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drink_id", nullable = true)
    private SoftDrink drink;

    @OneToMany(mappedBy = "orderItem",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<OrderItemIngredient> orderItemIngredients= new ArrayList<>();


    public String getDisplayName() {
        return itemType == ItemType.FOOD ? food.getName() : drink.getName();
    }

    public Long getItemId() {
        return itemType == ItemType.FOOD ? food.getId() : drink.getId();
    }



    public String getItemTypeString() {
        if (this.itemType != null) {
            return this.itemType.getJsonValue();
        }

        if (this.drink != null) {
            return "DRINK";
        }
        if (this.food != null) {
            return "FOOD";
        }

        return "FOOD";
    }



}
