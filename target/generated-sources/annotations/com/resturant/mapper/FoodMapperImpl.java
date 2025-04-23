package com.resturant.mapper;

import com.resturant.dto.FoodDTO;
import com.resturant.dto.FoodRequestDTO;
import com.resturant.dto.FoodResponseDTO;
import com.resturant.entity.Food;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-23T00:22:53-0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 1.8.0_265 (Eclipse OpenJ9)"
)
@Component
public class FoodMapperImpl implements FoodMapper {

    @Override
    public FoodDTO toDTO(Food food) {
        if ( food == null ) {
            return null;
        }

        FoodDTO foodDTO = new FoodDTO();

        foodDTO.setId( food.getId() );
        foodDTO.setName( food.getName() );
        foodDTO.setPrice( food.getPrice() );
        foodDTO.setDescription( food.getDescription() );
        foodDTO.setImagePath( food.getImagePath() );

        return foodDTO;
    }

    @Override
    public Food toEntity(FoodDTO foodDTO) {
        if ( foodDTO == null ) {
            return null;
        }

        Food food = new Food();

        food.setId( foodDTO.getId() );
        food.setName( foodDTO.getName() );
        food.setPrice( foodDTO.getPrice() );
        food.setDescription( foodDTO.getDescription() );
        food.setImagePath( foodDTO.getImagePath() );

        return food;
    }

    @Override
    public Set<FoodDTO> toDtoSet(Set<Food> foods) {
        if ( foods == null ) {
            return null;
        }

        Set<FoodDTO> set = new LinkedHashSet<FoodDTO>( Math.max( (int) ( foods.size() / .75f ) + 1, 16 ) );
        for ( Food food : foods ) {
            set.add( toDTO( food ) );
        }

        return set;
    }

    @Override
    public List<FoodDTO> toDTOList(List<Food> foodList) {
        if ( foodList == null ) {
            return null;
        }

        List<FoodDTO> list = new ArrayList<FoodDTO>( foodList.size() );
        for ( Food food : foodList ) {
            list.add( toDTO( food ) );
        }

        return list;
    }

    @Override
    public List<Food> toEntityList(List<FoodDTO> foodDTOList) {
        if ( foodDTOList == null ) {
            return null;
        }

        List<Food> list = new ArrayList<Food>( foodDTOList.size() );
        for ( FoodDTO foodDTO : foodDTOList ) {
            list.add( toEntity( foodDTO ) );
        }

        return list;
    }

    @Override
    public Food toEntityFromRequest(FoodRequestDTO requestDTO) {
        if ( requestDTO == null ) {
            return null;
        }

        Food food = new Food();

        food.setImagePath( requestDTO.getImagePath() );
        food.setName( requestDTO.getName() );
        food.setPrice( requestDTO.getPrice() );
        food.setDescription( requestDTO.getDescription() );

        return food;
    }

    @Override
    public FoodResponseDTO toResponseDTO(Food food) {
        if ( food == null ) {
            return null;
        }

        FoodResponseDTO foodResponseDTO = new FoodResponseDTO();

        foodResponseDTO.setId( food.getId() );
        foodResponseDTO.setName( food.getName() );
        foodResponseDTO.setPrice( food.getPrice() );
        foodResponseDTO.setDescription( food.getDescription() );
        foodResponseDTO.setImagePath( food.getImagePath() );

        foodResponseDTO.setIngredientNames( mapIngredientNames(food.getFoodIngredients()) );

        enhanceResponseDTO( food, foodResponseDTO );

        return foodResponseDTO;
    }

    @Override
    public void updateFromRequest(FoodRequestDTO requestDTO, Food food) {
        if ( requestDTO == null ) {
            return;
        }

        food.setName( requestDTO.getName() );
        food.setPrice( requestDTO.getPrice() );
        food.setDescription( requestDTO.getDescription() );
        food.setImagePath( requestDTO.getImagePath() );
    }
}
