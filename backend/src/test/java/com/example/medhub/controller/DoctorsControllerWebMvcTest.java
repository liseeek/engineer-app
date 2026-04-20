package com.example.medhub.controller;

import com.example.medhub.dto.request.OperationType;
import com.example.medhub.dto.request.UpdateDoctorLocationRequestDto;
import com.example.medhub.service.DoctorCrudService;
import com.example.medhub.service.DoctorSignupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DoctorsControllerWebMvcTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private DoctorCrudService doctorCrudService;

    @Mock
    private DoctorSignupService doctorSignupService;

    @BeforeEach
    void setUp() {
        DoctorsController controller = new DoctorsController(doctorCrudService, doctorSignupService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void updateDoctorLocation_returnsOk() throws Exception {
        UpdateDoctorLocationRequestDto request = new UpdateDoctorLocationRequestDto(2L, OperationType.REMOVE);

        mockMvc.perform(patch("/v1/doctors/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(doctorCrudService).updateDoctorLocation(anyLong(), any(UpdateDoctorLocationRequestDto.class));
    }

    @Test
    void deleteDoctor_returnsNoContent() throws Exception {
        doNothing().when(doctorCrudService).deleteById(7L);

        mockMvc.perform(delete("/v1/doctors/7"))
                .andExpect(status().isNoContent());

        verify(doctorCrudService).deleteById(7L);
    }
}
