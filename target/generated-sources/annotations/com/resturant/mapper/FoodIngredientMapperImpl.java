package com.resturant.mapper;

import com.resturant.dto.FoodIngredientDTO;
import com.resturant.entity.FoodIngredient;
import com.resturant.entity.FoodIngredientId;
import com.resturant.entity.Ingredient;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-18T12:56:07-0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 1.8.0_265 (Eclipse OpenJ9)"
)
@Component
public class FoodIngredientMapperImpl implements FoodIngredientMapper {

    @Override
    public FoodIngredientDTO toDTO(FoodIngredient foodIngredient) {
        if ( foodIngredient == null ) {
            return null;
        }

        FoodIngredientDTO foodIngredientDTO = new FoodIngredientDTO();

        foodIngredientDTO.setFoodId( foodIngredientIdFoodId( foodIngredient ) );
        foodIngredientDTO.setIngredientId( foodIngredientIdIngredientId( foodIngredient ) );
        foodIngredientDTO.setIngredientName( foodIngredientIngredientName( foodIngredient ) );
        foodIngredientDTO.setExtraCost( foodIngredient.getExtraCost() );

        return foodIngredientDTO;
    }

    @Override
    public FoodIngredient toEntity(FoodIngredientDTO foodIngredientDTO) {
        if ( foodIngredientDTO == null ) {
            return null;
        }

        FoodIngredient foodIngredient = new FoodIngredient();

        foodIngredient.setExtraCost( foodIngredientDTO.getExtraCost() );

        return foodIngredient;
    }

    private Long foodIngredientIdFoodId(FoodIngredient foodIngredient) {
        if ( foodIngredient == null ) {
            return null;
        }
        FoodIngredientId id = foodIngredient.getId();
        if ( id == null ) {
            return null;
        }
        Long foodId = id.getFoodId();
        if ( foodId == null ) {
            return null;
        }
        return foodId;
    }

    private Long foodIngredientIdIngredientId(FoodIngredient foodIngredient) {
        if ( foodIngredient == null ) {
            return null;
        }
        FoodIngredientId id = foodIngredient.getId();
        if ( id == null ) {
            return null;
        }
        Long ingredientId = id.getIngredientId();
        if ( ingredientId == null ) {
            return null;
        }
        return ingredientId;
    }

    private String foodIngredientIngredientName(FoodIngredient foodIngredient) {
        if ( foodIngredient == null ) {
            return null;
        }
        Ingredient ingredient = foodIngredient.getIngredient();
        if ( ingredient == null ) {
            return null;
        }
        String name = ingredient.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
