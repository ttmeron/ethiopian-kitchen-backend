package com.resturant.mapper;

import com.resturant.dto.FoodDTO;
import com.resturant.dto.UserDTO;
import com.resturant.entity.Food;
import com.resturant.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "password", source = "password")
    UserDTO toDTO(User user);
    @Mapping(target = "password", source = "password")
    User toEntity(UserDTO userDTO);

}
