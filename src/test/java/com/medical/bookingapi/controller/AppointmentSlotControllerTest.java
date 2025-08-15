package com.medical.bookingapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.bookingapi.dto.AppointmentSlotDTO;
import com.medical.bookingapi.service.AppointmentSlotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AppointmentSlotController.class,
    excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE,
                             classes = com.medical.bookingapi.security.JwtFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
class AppointmentSlotControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;

  @MockBean AppointmentSlotService appointmentSlotService;

  @Test
  void getSlotsByDoctor_shouldReturn200_andJsonArray() throws Exception {
    when(appointmentSlotService.findByDoctorId(1L))
        .thenReturn(List.of(new AppointmentSlotDTO()));

    mvc.perform(get("/api/appointmentSlots/by-doctor/{doctorId}", 1))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0]").exists());
  }

  @Test
  void getAvailableSlots_shouldReturn200() throws Exception {
    when(appointmentSlotService.findByIsBookedFalse())
        .thenReturn(List.of(new AppointmentSlotDTO()));

    mvc.perform(get("/api/appointmentSlots/available"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  void getSlotById_whenMissing_shouldReturn404() throws Exception {
    when(appointmentSlotService.findById(99L)).thenReturn(Optional.empty());

    mvc.perform(get("/api/appointmentSlots/{id}", 99))
        .andExpect(status().isNotFound());
  }

  @Test
  void getDoctorAvailableSlots_shouldReturn200() throws Exception {
    when(appointmentSlotService.findByDoctorAndIsBookedFalse(7L))
        .thenReturn(List.of(new AppointmentSlotDTO()));

    mvc.perform(get("/api/appointmentSlots/by-doctor/{doctorId}/available", 7))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  void getSlotsBetween_shouldReturn200_withIsoParams() throws Exception {
    // Use ISO date-time (e.g., 2025-08-15T10:00:00) per @DateTimeFormat(iso = ISO.DATE_TIME)
    String start = "2025-08-15T10:00:00";
    String end   = "2025-08-16T10:00:00";

    when(appointmentSlotService.findByStartTimeBetween(
        LocalDateTime.parse(start), LocalDateTime.parse(end)))
        .thenReturn(List.of(new AppointmentSlotDTO()));

    mvc.perform(get("/api/appointmentSlots/between")
            .param("start", start)
            .param("end", end))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  void getDoctorFirstAvailableSorted_whenMissing_shouldReturn404() throws Exception {
    when(appointmentSlotService.findFirstByDoctorAndIsBookedFalseOrderByStartTimeAsc(5L))
        .thenReturn(Optional.empty());

    mvc.perform(get("/api/appointmentSlots/by-doctor/{doctorId}/available/sorted", 5))
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteSlot_shouldReturn204_andDelegate() throws Exception {
    mvc.perform(delete("/api/appointmentSlots/{id}", 12))
        .andExpect(status().isNoContent());

    verify(appointmentSlotService).deleteSlot(12L);
  }
}
