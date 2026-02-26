package com.resturant.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.resturant.entity.OrderItem;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Individual item within an order")
public class OrderItemDTO {


    @Schema(
            description = "Unique identifier of the order item",
            example = "105",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long orderItemId;
    @Schema(
            description = "ID of the food item",
            example = "5",
            required = true
    )
    @JsonProperty("foodId")
    private Long foodId;

    private Long drinkId;
    private String size;

    private String iceOption;


    @Schema(
            description = "Quantity ordered",
            example = "2",
            required = true
    )
    @NotNull
    @Positive
    private int quantity;

    @Schema(
            description = "Name of the Ethiopian food item",
            example = "Doro Wot",
            required = true
    )
    private String foodName;

    @Schema(description = "Name of the drink item", example = "Pepsi")
    private String drinkName;


    @JsonProperty("itemType")
    private String itemType;

    @Schema(
            description = "Price per unit in Ethiopian Birr",
            example = "250.00",
            required = true
    )
    private BigDecimal price;
    @ArraySchema(
            arraySchema = @Schema(
                    description = "Customizations to ingredients in this order item",
                    example = "[{\"ingredientId\": 1, \"modification\": \"EXTRA\"}]"
            ),
            schema = @Schema(implementation = OrderItemIngredientDTO.class)
    )
    private List<OrderItemIngredientDTO> customIngredients;

    @JsonProperty("foodId")          // Forces JSON to use `foodId` as "id"
    public Long getFoodId() {
        return foodId;
    }

    @JsonIgnore
    public OrderItem.ItemType getItemTypeEnum() {
        if (itemType == null) return null;
        return OrderItem.ItemType.valueOf(itemType.toUpperCase());
    }
    @JsonIgnore
    public void setItemTypeEnum(OrderItem.ItemType enumType) {
        if (enumType != null) {
            this.itemType = enumType.name();
        }
    }

    public static OrderItemDTO createFoodItem(Long foodId, String foodName, int quantity, BigDecimal price) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setFoodId(foodId);
        dto.setFoodName(foodName);
        dto.setQuantity(quantity);
        dto.setPrice(price);
        dto.setItemType("FOOD");
        return dto;
    }

    public static OrderItemDTO createDrinkItem(Long drinkId, String drinkName, int quantity, BigDecimal price, String size, String iceOption) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setDrinkId(drinkId);
        dto.setDrinkName(drinkName);
        dto.setQuantity(quantity);
        dto.setPrice(price);
        dto.setSize(size);
        dto.setIceOption(iceOption);
        dto.setItemType("DRINK");
        return dto;
    }

    @JsonProperty("itemType")
    public String getItemTypeString() {
        return itemType;
    }
    public boolean isFoodItem() {
    return itemType != null && itemType.equalsIgnoreCase("FOOD");
}

    public boolean isDrinkItem() {
        return itemType != null && itemType.equalsIgnoreCase("DRINK");
    }


}
