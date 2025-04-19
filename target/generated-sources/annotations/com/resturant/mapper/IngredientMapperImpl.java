package com.resturant.mapper;

import com.resturant.dto.IngredientDTO;
import com.resturant.entity.Ingredient;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-18T12:56:07-0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 1.8.0_265 (Eclipse OpenJ9)"
)
@Component
public class IngredientMapperImpl implements IngredientMapper {

    @Override
    public IngredientDTO toDTO(Ingredient ingredient) {
        if ( ingredient == null ) {
            return null;
        }

        IngredientDTO ingredientDTO = new IngredientDTO();

        ingredientDTO.setId( ingredient.getId() );
        ingredientDTO.setName( ingredient.getName() );
        ingredientDTO.setExtraCost( ingredient.getExtraCost() );

        return ingredientDTO;
    }

    @Override
    public Ingredient toEntity(IngredientDTO ingredientDTO) {
        if ( ingredientDTO == null ) {
            return null;
        }

        Ingredient ingredient = new Ingredient();

        ingredient.setId( ingredientDTO.getId() );
        ingredient.setName( ingredientDTO.getName() );
        ingredient.setExtraCost( ingredientDTO.getExtraCost() );

        return ingredient;
    }

    @Override
    public List<IngredientDTO> toDTOList(List<Ingredient> ingredientList) {
        if ( ingredientList == null ) {
            return null;
        }

        List<IngredientDTO> list = new ArrayList<IngredientDTO>( ingredientList.size() );
        for ( Ingredient ingredient : ingredientList ) {
            list.add( toDTO( ingredient ) );
        }

        return list;
    }

    @Override
    public List<Ingredient> toEntityList(List<IngredientDTO> dtos) {
        if ( dtos == null ) {
            return null;
        }

        List<Ingredient> list = new ArrayList<Ingredient>( dtos.size() );
        for ( IngredientDTO ingredientDTO : dtos ) {
            list.add( toEntity( ingredientDTO ) );
        }

        return list;
    }
}
