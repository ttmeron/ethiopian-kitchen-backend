package com.resturant.mapper;

import com.resturant.dto.DeliveryDTO;
import com.resturant.entity.Delivery;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeliveryMapper {

    DeliveryMapper INSTANCE = Mappers.getMapper(DeliveryMapper.class);

    DeliveryDTO toDTO(Delivery delivery);
    Delivery toEntity(DeliveryDTO deliveryDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    void updateDeliveryFromDTO(DeliveryDTO dto, @MappingTarget Delivery entity);
}
