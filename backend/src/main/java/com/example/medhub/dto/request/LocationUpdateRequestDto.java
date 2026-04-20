package com.example.medhub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Year;

@Getter
@Setter
@NoArgsConstructor
public class LocationUpdateRequestDto {

    @Size(max = 2000)
    private String description;

    @Min(1800)
    @Max(2100)
    private Integer yearEstablished;

    @Size(max = 20)
    private String phoneNumber;

    @Email
    @Size(max = 100)
    private String email;

    public Integer getYearEstablishedValidated() {
        if (yearEstablished != null && yearEstablished > Year.now().getValue()) {
            throw new IllegalArgumentException("Year established cannot be in the future.");
        }
        return yearEstablished;
    }
}
