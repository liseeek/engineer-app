package com.example.medhub.mapper;

import com.example.medhub.dto.request.UserCreateRequestDto;
import com.example.medhub.dto.UserDto;
import com.example.medhub.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "password", source = "encryptedPassword")
    @Mapping(target = "authority", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    UserEntity toUser(UserCreateRequestDto createRequestDto, String encryptedPassword);

    UserDto toUserDto(UserEntity savedUser);
}
