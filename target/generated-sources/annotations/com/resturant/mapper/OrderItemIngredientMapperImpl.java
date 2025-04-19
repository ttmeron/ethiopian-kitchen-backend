package com.resturant.mapper;

import com.resturant.dto.OrderItemIngredientDTO;
import com.resturant.entity.Ingredient;
import com.resturant.entity.OrderItemIngredient;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-18T12:58:02-0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 1.8.0_265 (Eclipse OpenJ9)"
)
@Component
public class OrderItemIngredientMapperImpl implements OrderItemIngredientMapper {

    @Override
    public OrderItemIngredientDTO toDTO(OrderItemIngredient orderItemIngredient) {
        if ( orderItemIngredient == null ) {
            return null;
        }

        OrderItemIngredientDTO orderItemIngredientDTO = new OrderItemIngredientDTO();

        orderItemIngredientDTO.setIngredientId( orderItemIngredientIngredientId( orderItemIngredient ) );
        orderItemIngredientDTO.setIngredientName( orderItemIngredientIngredientName( orderItemIngredient ) );
        orderItemIngredientDTO.setId( orderItemIngredient.getId() );
        orderItemIngredientDTO.setExtraCost( orderItemIngredient.getExtraCost() );
        orderItemIngredientDTO.setQuantity( orderItemIngredient.getQuantity() );

        return orderItemIngredientDTO;
    }

    @Override
    public OrderItemIngredient toEntity(OrderItemIngredientDTO orderItemIngredientDTO) {
        if ( orderItemIngredientDTO == null ) {
            return null;
        }

        OrderItemIngredient orderItemIngredient = new OrderItemIngredient();

        orderItemIngredient.setIngredient( map( orderItemIngredientDTO.getIngredientId() ) );
        orderItemIngredient.setId( orderItemIngredientDTO.getId() );
        orderItemIngredient.setQuantity( orderItemIngredientDTO.getQuantity() );
        orderItemIngredient.setExtraCost( orderItemIngredientDTO.getExtraCost() );

        return orderItemIngredient;
    }

    @Override
    public List<OrderItemIngredientDTO> toDTOList(List<OrderItemIngredient> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderItemIngredientDTO> list1 = new ArrayList<OrderItemIngredientDTO>( list.size() );
        for ( OrderItemIngredient orderItemIngredient : list ) {
            list1.add( toDTO( orderItemIngredient ) );
        }

        return list1;
    }

    @Override
    public List<OrderItemIngredient> toEntityList(List<OrderItemIngredientDTO> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderItemIngredient> list1 = new ArrayList<OrderItemIngredient>( list.size() );
        for ( OrderItemIngredientDTO orderItemIngredientDTO : list ) {
            list1.add( toEntity( orderItemIngredientDTO ) );
        }

        return list1;
    }

    private Long orderItemIngredientIngredientId(OrderItemIngredient orderItemIngredient) {
        if ( orderItemIngredient == null ) {
            return null;
        }
        Ingredient ingredient = orderItemIngredient.getIngredient();
        if ( ingredient == null ) {
            return null;
        }
        Long id = ingredient.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String orderItemIngredientIngredientName(OrderItemIngredient orderItemIngredient) {
        if ( orderItemIngredient == null ) {
            return null;
        }
        Ingredient ingredient = orderItemIngredient.getIngredient();
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
