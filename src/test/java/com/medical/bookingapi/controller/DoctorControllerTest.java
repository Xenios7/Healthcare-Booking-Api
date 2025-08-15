package com.medical.bookingapi.controller;

import com.medical.bookingapi.dto.DoctorDTO;
import com.medical.bookingapi.service.DoctorService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = DoctorController.class,
    excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE,
                             classes = com.medical.bookingapi.security.JwtFilter.class)
)
@AutoConfigureMockMvc(addFilters = false) // don't run security filters during requests
class DoctorControllerTest {

  @Autowired MockMvc mvc;

  @MockBean DoctorService doctorService;

  @Test
  void getDoctorById_shouldReturn200_andBody() throws Exception {
    when(doctorService.findById(1L)).thenReturn(new DoctorDTO());

    mvc.perform(get("/api/doctors/{id}", 1))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").exists());

    verify(doctorService).findById(1L);
  }

  @Test
  void getDoctorByEmail_shouldReturn200() throws Exception {
    when(doctorService.findByEmail("doc@example.com")).thenReturn(new DoctorDTO());

    mvc.perform(get("/api/doctors/email").param("email", "doc@example.com"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").exists());

    verify(doctorService).findByEmail("doc@example.com");
  }

  @Test
  void getDoctorsBySpeciality_shouldReturn200_andList() throws Exception {
    when(doctorService.findBySpeciality("CARDIOLOGY"))
        .thenReturn(List.of(new DoctorDTO()));

    mvc.perform(get("/api/doctors/speciality/{speciality}", "CARDIOLOGY"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0]").exists());

    verify(doctorService).findBySpeciality("CARDIOLOGY");
  }

  @Test
  void me_shouldReturn200() throws Exception {
    when(doctorService.me()).thenReturn(new DoctorDTO());

    mvc.perform(get("/api/doctors/me"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").exists());

    verify(doctorService).me();
  }
}
