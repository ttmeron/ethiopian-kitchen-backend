package com.resturant.mapper;

import com.resturant.dto.SoftDrinkDTO;
import com.resturant.entity.SoftDrink;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SoftDrinkMapper {

    SoftDrinkMapper INSTANCE = Mappers.getMapper(SoftDrinkMapper.class);

    SoftDrink toEntity(SoftDrinkDTO softDrinkDTO);


    SoftDrinkDTO toDto(SoftDrink softDrink);

    List<SoftDrinkDTO> toDtoList(List<SoftDrink> softDrinks);
    List<SoftDrink> toEntityList(List<SoftDrinkDTO> softDrinkDTOs);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(SoftDrinkDTO dto, @MappingTarget SoftDrink entity);

}
