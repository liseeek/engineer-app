package com.example.medhub.dto;

import com.example.medhub.enums.Authority;

public record UserListItemDto(Long userId, String name, String surname, String email, Authority authority) {
}
