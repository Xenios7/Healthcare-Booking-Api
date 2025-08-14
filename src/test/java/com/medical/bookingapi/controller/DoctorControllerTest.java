package com.medical.bookingapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DoctorController.class)
@AutoConfigureMockMvc(addFilters = false) // skip JwtFilter
class DoctorControllerTest {

  @Autowired MockMvc mvc;

  // @MockBean any collaborators/services used by the controller
  // e.g. @MockBean AdminService adminService;

  @Test
  void ping_shouldReturn200() throws Exception {
    mvc.perform(get("/actuator/health")).andExpect(status().isOk());
  }
}
