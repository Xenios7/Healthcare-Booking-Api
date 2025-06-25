package com.medical.bookingapi.mapper;

import com.medical.bookingapi.dto.UserResponseDTO;
import com.medical.bookingapi.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "name", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "token", ignore = true)
    UserResponseDTO toDto(User user);

}
