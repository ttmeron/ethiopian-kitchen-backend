package com.resturant.mapper;

import com.resturant.dto.OrderDTO;
import com.resturant.entity.Order;
import com.resturant.entity.OrderStatus;
import com.resturant.entity.User;
import java.util.Collections;
import javax.annotation.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-18T12:56:07-0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 1.8.0_265 (Eclipse OpenJ9)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private DeliveryMapper deliveryMapper;

    @Override
    public OrderDTO toDTO(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderDTO orderDTO = new OrderDTO();

        orderDTO.setUserName( orderUserUserName( order ) );
        orderDTO.setEmail( orderUserEmail( order ) );
        orderDTO.setDeliveryDTO( deliveryMapper.toDTO( order.getDelivery() ) );
        orderDTO.setTotalPrice( order.getTotalPrice() );
        if ( order.getStatus() != null ) {
            orderDTO.setStatus( order.getStatus().name() );
        }
        orderDTO.setOrderItems( orderItemMapper.toDTOList( order.getOrderItems() ) );

        return orderDTO;
    }

    @Override
    public Order toEntity(OrderDTO orderDTO) {
        if ( orderDTO == null ) {
            return null;
        }

        Order order = new Order();

        order.setTotalPrice( orderDTO.getTotalPrice() );
        if ( orderDTO.getStatus() != null ) {
            order.setStatus( Enum.valueOf( OrderStatus.class, orderDTO.getStatus() ) );
        }

        return order;
    }

    private String orderUserUserName(Order order) {
        if ( order == null ) {
            return null;
        }
        User user = order.getUser();
        if ( user == null ) {
            return null;
        }
        String userName = user.getUserName();
        if ( userName == null ) {
            return null;
        }
        return userName;
    }

    private String orderUserEmail(Order order) {
        if ( order == null ) {
            return null;
        }
        User user = order.getUser();
        if ( user == null ) {
            return null;
        }
        String email = user.getEmail();
        if ( email == null ) {
            return null;
        }
        return email;
    }
}
