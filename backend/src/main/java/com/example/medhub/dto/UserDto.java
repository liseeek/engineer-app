package com.example.medhub.dto;

import com.example.medhub.entity.Authority;
import com.example.medhub.entity.UserEntity;
import com.example.medhub.mapper.UserMapper;

public record UserDto(Long userId, String name, String surname, String email, String password, String phoneNumber,
                      Authority authority) {
    public static UserDto from(UserEntity userEntity) {
        return UserMapper.USER_MAPPER.toUserDto(userEntity);
    }
}
