package com.example.medhub.controller;

import com.example.medhub.service.AppointmentsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AppointmentsControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private AppointmentsService appointmentsService;

    @BeforeEach
    void setUp() {
        AppointmentsController controller = new AppointmentsController(appointmentsService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void addAppointment_returnsCreated() throws Exception {
        mockMvc.perform(patch("/v1/appointments/10"))
                .andExpect(status().isCreated());

        verify(appointmentsService).addAppointmentToUser(10L);
    }

    @Test
    void cancelAppointment_returnsNoContent() throws Exception {
        mockMvc.perform(patch("/v1/appointments/10/cancel"))
                .andExpect(status().isNoContent());

        verify(appointmentsService).cancelAppointment(10L);
    }

    @Test
    void completeAppointment_returnsNoContent() throws Exception {
        mockMvc.perform(patch("/v1/appointments/10/complete"))
                .andExpect(status().isNoContent());

        verify(appointmentsService).completeAppointment(10L);
    }
}
