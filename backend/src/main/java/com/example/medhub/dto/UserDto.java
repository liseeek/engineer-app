package com.example.medhub.dto;

import com.example.medhub.enums.Authority;

public record UserDto(Long userId, String name, String surname, String email, String password, String phoneNumber,
                      Authority authority) {
}
