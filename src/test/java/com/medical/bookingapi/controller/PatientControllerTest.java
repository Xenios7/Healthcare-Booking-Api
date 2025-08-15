package com.medical.bookingapi.controller;

import com.medical.bookingapi.dto.PatientDTO;
import com.medical.bookingapi.service.PatientService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = PatientController.class,
    excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE,
                             classes = com.medical.bookingapi.security.JwtFilter.class)
)
@AutoConfigureMockMvc(addFilters = false) // don't run security filters
class PatientControllerTest {

  @Autowired MockMvc mvc;

  @MockBean PatientService patientService;

  @Test
  void getPatientById_shouldReturn200_andBody() throws Exception {
    when(patientService.getPatientById(1L)).thenReturn(new PatientDTO());

    mvc.perform(get("/api/patients/{id}", 1))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").exists());

    verify(patientService).getPatientById(1L);
  }

  @Test
  void getPatientByEmail_shouldReturn200_andBody() throws Exception {
    when(patientService.findByEmail("pat@example.com"))
        .thenReturn(Optional.of(new PatientDTO()));

    mvc.perform(get("/api/patients/email").param("email", "pat@example.com"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").exists());

    verify(patientService).findByEmail("pat@example.com");
  }

  @Test
  void getPatientsByBloodType_shouldReturn200_andList() throws Exception {
    when(patientService.findByBloodType("O+"))
        .thenReturn(List.of(new PatientDTO()));

    mvc.perform(get("/api/patients/blood-type/{bloodType}", "O+"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0]").exists());

    verify(patientService).findByBloodType("O+");
  }

  @Test
  void me_shouldReturn200_andBody() throws Exception {
    when(patientService.me()).thenReturn(new PatientDTO());

    mvc.perform(get("/api/patients/me"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").exists());

    verify(patientService).me();
  }
}
