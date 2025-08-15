package com.medical.bookingapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.bookingapi.auth.AuthService;
import com.medical.bookingapi.dto.*;
import com.medical.bookingapi.security.JwtFilter;
import com.medical.bookingapi.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false) // skip JwtFilter & other security filters
class AdminControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;

  // Collaborators mocked
  @MockBean AuthService authService;
  @MockBean AdminService adminService;

  // Satisfy context if your app registers these beans
  @MockBean JwtFilter jwtFilter;

  // ----------- Provisioning endpoints (require ADMIN role) -----------

  @Test
  @WithMockUser(roles = "ADMIN")
  void createDoctor_shouldReturn201_andDelegateToAuthService() throws Exception {
    String json = """
      {
        "firstName": "Gregory",
        "lastName": "House",
        "email": "house@example.com",
        "password": "Secret123!",
        "speciality": "Cardiology",
        "location": "Nicosia General",
        "licenseNumber": "CY-12345"
      }
    """;

    mvc.perform(post("/api/admins/doctors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isCreated())
        .andExpect(content().string("Doctor created successfully."));

    verify(authService, times(1)).registerDoctor(any(DoctorRegistrationDTO.class));
  }


  @Test
  @WithMockUser(roles = "ADMIN")
  void createAdmin_shouldReturn201_andDelegateToAuthService() throws Exception {
    String json = """
      {
        "firstName": "Ada",
        "lastName": "Lovelace",
        "email": "ada@example.com",
        "password": "Secret123!",
        "role": "ADMIN"
      }
    """;

    mvc.perform(post("/api/admins/admins")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isCreated())
        .andExpect(content().string("Admin created successfully."));

    verify(authService, times(1)).registerAdmin(any(RegisterRequestDTO.class));
  }


  @Test
  @WithMockUser(roles = "ADMIN")
  void ping_shouldReturn200() throws Exception {
    mvc.perform(get("/api/admins/ping"))
        .andExpect(status().isOk())
        .andExpect(content().string("admin ok"));
  }

  // ----------- Patient admin endpoints -----------

  @Test
  @WithMockUser(roles = "ADMIN")
  void updatePatient_shouldReturn200_withDto() throws Exception {
    Long id = 42L;

    PatientAdminUpdateDTO update = new PatientAdminUpdateDTO();
    // e.g. update.setBloodType("O+");
    String body = objectMapper.writeValueAsString(update);

    PatientDTO returned = new PatientDTO();
    // set a couple of fields so jsonPath assertions make sense
    // e.g. returned.setId(id); returned.setBloodType("O+");
    when(adminService.updatePatient(eq(id), any(PatientAdminUpdateDTO.class))).thenReturn(returned);

    mvc.perform(put("/api/admins/patients/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        // Optionally: .andExpect(jsonPath("$.id").value(42))

    verify(adminService, times(1)).updatePatient(eq(id), any(PatientAdminUpdateDTO.class));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deletePatient_shouldReturn200_andDelegate() throws Exception {
    Long id = 5L;

    mvc.perform(delete("/api/admins/patients/{id}", id))
        .andExpect(status().isOk()); // your controller returns void → 200 OK

    verify(adminService, times(1)).deletePatient(id);
  }

  // ----------- Doctor admin endpoints -----------

  @Test
  @WithMockUser(roles = "ADMIN")
  void updateDoctor_shouldReturn200_withDto() throws Exception {
    Long id = 7L;

    DoctorAdminUpdateDTO update = new DoctorAdminUpdateDTO();
    // e.g. update.setSpecialty("Cardiology"); update.setLocation("Nicosia");
    String body = objectMapper.writeValueAsString(update);

    DoctorDTO returned = new DoctorDTO();
    // e.g. returned.setId(id);
    when(adminService.updateDoctor(eq(id), any(DoctorAdminUpdateDTO.class))).thenReturn(returned);

    mvc.perform(put("/api/admins/doctors/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

    verify(adminService, times(1)).updateDoctor(eq(id), any(DoctorAdminUpdateDTO.class));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deleteDoctor_shouldReturn200_andDelegate() throws Exception {
    Long id = 9L;

    mvc.perform(delete("/api/admins/doctors/{id}", id))
        .andExpect(status().isOk()); // controller returns void → 200

    verify(adminService, times(1)).deleteDoctor(id);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createAdmin_withInvalidBody_shouldReturn400() throws Exception {
    // Missing required fields
    String invalid = "{}";

    mvc.perform(post("/api/admins/admins")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalid))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(authService);
  }

}
