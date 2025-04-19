package com.resturant.mapper;

import com.resturant.dto.DeliveryDTO;
import com.resturant.entity.Delivery;
import com.resturant.entity.DeliveryStatus;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-19T12:51:49-0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 1.8.0_265 (Eclipse OpenJ9)"
)
@Component
public class DeliveryMapperImpl implements DeliveryMapper {

    @Override
    public DeliveryDTO toDTO(Delivery delivery) {
        if ( delivery == null ) {
            return null;
        }

        DeliveryDTO deliveryDTO = new DeliveryDTO();

        deliveryDTO.setId( delivery.getId() );
        deliveryDTO.setDeliveryAddress( delivery.getDeliveryAddress() );
        deliveryDTO.setDeliveryTime( delivery.getDeliveryTime() );
        if ( delivery.getStatus() != null ) {
            deliveryDTO.setStatus( delivery.getStatus().name() );
        }

        return deliveryDTO;
    }

    @Override
    public Delivery toEntity(DeliveryDTO deliveryDTO) {
        if ( deliveryDTO == null ) {
            return null;
        }

        Delivery.DeliveryBuilder delivery = Delivery.builder();

        delivery.id( deliveryDTO.getId() );
        delivery.deliveryAddress( deliveryDTO.getDeliveryAddress() );
        delivery.deliveryTime( deliveryDTO.getDeliveryTime() );
        if ( deliveryDTO.getStatus() != null ) {
            delivery.status( Enum.valueOf( DeliveryStatus.class, deliveryDTO.getStatus() ) );
        }

        return delivery.build();
    }

    @Override
    public void updateDeliveryFromDTO(DeliveryDTO dto, Delivery entity) {
        if ( dto == null ) {
            return;
        }

        entity.setDeliveryAddress( dto.getDeliveryAddress() );
        entity.setDeliveryTime( dto.getDeliveryTime() );
        if ( dto.getStatus() != null ) {
            entity.setStatus( Enum.valueOf( DeliveryStatus.class, dto.getStatus() ) );
        }
        else {
            entity.setStatus( null );
        }
    }
}
