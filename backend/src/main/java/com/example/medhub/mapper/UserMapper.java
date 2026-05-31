package com.example.medhub.mapper;
 
import com.example.medhub.dto.request.UserCreateRequestDto;
import com.example.medhub.dto.response.UserDto;
import com.example.medhub.dto.response.UserListItemDto;
import com.example.medhub.entity.Patient;
import com.example.medhub.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
 
@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "password", source = "encryptedPassword")
    @Mapping(target = "authority", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "pesel", source = "createRequestDto.pesel")
    @Mapping(target = "peselHash", ignore = true)
    @Mapping(target = "locked", ignore = true)
    Patient toUser(UserCreateRequestDto createRequestDto, String encryptedPassword);
 
    UserDto toUserDto(Patient savedUser);
 
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "surname", source = "user.surname")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "authority", source = "user.authority")
    @Mapping(target = "locked", source = "user.locked")
    UserListItemDto toUserListItem(User user);
}
