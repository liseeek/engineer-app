package com.example.medhub.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@NoArgsConstructor
public class DoctorOwnProfileUpdateRequestDto {

    @Size(max = 2000)
    private String bio;

    @URL
    @Size(max = 255)
    private String avatarUrl;
}
