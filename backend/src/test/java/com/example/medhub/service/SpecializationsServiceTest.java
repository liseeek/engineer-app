package com.example.medhub.service;

import com.example.medhub.dto.SpecializationDto;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.repository.SpecializationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpecializationsServiceTest {

    @Mock
    private SpecializationRepository specializationRepository;

    @InjectMocks
    private SpecializationsService specializationsService;

    @Test
    void getSpecializationsByCity_ShouldReturnOnlySpecializationsAvailableInGivenCity() {
        String city = "Warszawa";
        
        SpecializationEntity spec1 = new SpecializationEntity();
        spec1.setSpecializationId(1L);
        spec1.setSpecializationName("Kardiolog");

        when(specializationRepository.findDistinctByDoctors_Locations_City(city)).thenReturn(List.of(spec1));

        List<SpecializationDto> result = specializationsService.getSpecializationsByCity(city);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).specializationName()).isEqualTo("Kardiolog");
    }
}
