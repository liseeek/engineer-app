package com.example.medhub.entity;

import com.example.medhub.dto.request.UserCreateRequestDto;
import com.example.medhub.mapper.UserMapper;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserEntity extends User {

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppointmentsEntity> appointments;
    public static UserEntity from(UserCreateRequestDto newUser, String encryptedPassword) {
        return UserMapper.USER_MAPPER.toUser(newUser, encryptedPassword);
    }
}
