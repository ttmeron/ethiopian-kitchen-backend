package com.resturant.mapper;

import com.resturant.dto.OrderItemDTO;
import com.resturant.dto.OrderItemIngredientDTO;
import com.resturant.entity.Food;
import com.resturant.entity.OrderItem;
import com.resturant.entity.OrderItemIngredient;
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
public class OrderItemMapperImpl implements OrderItemMapper {

    @Override
    public OrderItemDTO toDTO(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }

        OrderItemDTO orderItemDTO = new OrderItemDTO();

        orderItemDTO.setFoodName( orderItemFoodName( orderItem ) );
        orderItemDTO.setFoodId( orderItemFoodId( orderItem ) );
        orderItemDTO.setCustomIngredients( orderItemIngredientListToOrderItemIngredientDTOList( orderItem.getOrderItemIngredients() ) );
        orderItemDTO.setQuantity( orderItem.getQuantity() );
        orderItemDTO.setPrice( orderItem.getPrice() );

        return orderItemDTO;
    }

    @Override
    public OrderItem toEntity(OrderItemDTO orderItemDTO) {
        if ( orderItemDTO == null ) {
            return null;
        }

        OrderItem orderItem = new OrderItem();

        orderItem.setQuantity( orderItemDTO.getQuantity() );
        orderItem.setPrice( orderItemDTO.getPrice() );

        return orderItem;
    }

    @Override
    public List<OrderItemDTO> toDTOList(List<OrderItem> orderItemList) {
        if ( orderItemList == null ) {
            return null;
        }

        List<OrderItemDTO> list = new ArrayList<OrderItemDTO>( orderItemList.size() );
        for ( OrderItem orderItem : orderItemList ) {
            list.add( toDTO( orderItem ) );
        }

        return list;
    }

    @Override
    public List<OrderItem> toEntityList(List<OrderItemDTO> orderItemDTOList) {
        if ( orderItemDTOList == null ) {
            return null;
        }

        List<OrderItem> list = new ArrayList<OrderItem>( orderItemDTOList.size() );
        for ( OrderItemDTO orderItemDTO : orderItemDTOList ) {
            list.add( toEntity( orderItemDTO ) );
        }

        return list;
    }

    private String orderItemFoodName(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }
        Food food = orderItem.getFood();
        if ( food == null ) {
            return null;
        }
        String name = food.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private Long orderItemFoodId(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }
        Food food = orderItem.getFood();
        if ( food == null ) {
            return null;
        }
        Long id = food.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected OrderItemIngredientDTO orderItemIngredientToOrderItemIngredientDTO(OrderItemIngredient orderItemIngredient) {
        if ( orderItemIngredient == null ) {
            return null;
        }

        OrderItemIngredientDTO orderItemIngredientDTO = new OrderItemIngredientDTO();

        orderItemIngredientDTO.setId( orderItemIngredient.getId() );
        orderItemIngredientDTO.setExtraCost( orderItemIngredient.getExtraCost() );
        orderItemIngredientDTO.setQuantity( orderItemIngredient.getQuantity() );

        return orderItemIngredientDTO;
    }

    protected List<OrderItemIngredientDTO> orderItemIngredientListToOrderItemIngredientDTOList(List<OrderItemIngredient> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderItemIngredientDTO> list1 = new ArrayList<OrderItemIngredientDTO>( list.size() );
        for ( OrderItemIngredient orderItemIngredient : list ) {
            list1.add( orderItemIngredientToOrderItemIngredientDTO( orderItemIngredient ) );
        }

        return list1;
    }
}
