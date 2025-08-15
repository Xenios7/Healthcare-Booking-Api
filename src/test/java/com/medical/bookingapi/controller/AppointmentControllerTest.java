package com.medical.bookingapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.bookingapi.dto.AppointmentCreateDTO;
import com.medical.bookingapi.dto.AppointmentDTO;
import com.medical.bookingapi.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = AppointmentController.class,
    excludeFilters = @Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = com.medical.bookingapi.security.JwtFilter.class // exclude security filter bean from MVC slice
    )
)
@AutoConfigureMockMvc(addFilters = false) // don't run remaining filters during requests
class AppointmentControllerTest {

    @Autowired MockMvc mvc;

    @MockBean AppointmentService appointmentService;

    @Test
    void getAppointmentsByDoctor_shouldReturn200_andBody() throws Exception {
        // If AppointmentDTO has a no-args ctor, this is fine; otherwise mock it:
        AppointmentDTO dtoItem = new AppointmentDTO(); // or: Mockito.mock(AppointmentDTO.class);
        List<AppointmentDTO> dto = List.of(dtoItem);
        when(appointmentService.findByDoctorId(1L)).thenReturn(dto);

        mvc.perform(get("/api/appointments/by-doctor/{doctorId}", 1))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    void getAppointmentBySlot_whenMissing_shouldReturn404() throws Exception {
        when(appointmentService.findBySlotId(77L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/appointments/by-slot/{slotId}", 77))
            .andExpect(status().isNotFound());
    }

    @Test
    void bookAppointment_shouldReturn200() throws Exception {
        // Build a valid create DTO for your @Valid constraints.
        // If you don't know the required fields yet, replace with Mockito.mock(...) for serialization,
        // or fill the fields your DTO marks as @NotNull/@NotBlank.
        AppointmentCreateDTO create = new AppointmentCreateDTO(); // fill required fields if any
        AppointmentDTO saved = new AppointmentDTO();              // return stub

        when(appointmentService.bookAppointment(any())).thenReturn(saved);

        mvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(create)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void updateStatus_shouldReturn200() throws Exception {
        when(appointmentService.updateStatus(5L, "CONFIRMED"))
            .thenReturn(new AppointmentDTO());

        mvc.perform(put("/api/appointments/{id}", 5).param("status", "CONFIRMED"))
            .andExpect(status().isOk());
    }

    @Test
    void deleteAppointment_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/appointments/{id}", 9))
            .andExpect(status().isNoContent());

        verify(appointmentService).deleteAppointment(9L);
    }
}
