package com.medical.bookingapi.auth;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.bookingapi.dto.RegisterRequestDTO;
import com.medical.bookingapi.dto.UserLoginDTO;
import com.medical.bookingapi.dto.UserResponseDTO;
import com.medical.bookingapi.security.JwtFilter;
import com.medical.bookingapi.security.JwtService;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // skip JwtFilter
class AuthControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper; // Provided by Spring Boot test starter
  @MockBean AuthService authService;
  @MockBean JwtFilter jwtFilter;
  @MockBean JwtService jwtService;

  @Test
void register_shouldReturn201_andDelegateToService() throws Exception {
  RegisterRequestDTO req = new RegisterRequestDTO();
  req.setFirstName("Pat");
  req.setLastName("Patient");
  req.setEmail("pat@example.com");
  req.setPassword("secret123");
  req.setRole("PATIENT"); // <-- the missing piece

  String json = objectMapper.writeValueAsString(req);

  mvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .characterEncoding("utf-8")
          .content(json))
      .andExpect(status().isCreated())
      .andExpect(content().string("Patient registered successfully."));

  ArgumentCaptor<RegisterRequestDTO> captor = ArgumentCaptor.forClass(RegisterRequestDTO.class);
  verify(authService, times(1)).registerPatient(captor.capture());
  assertThat(captor.getValue().getEmail()).isEqualTo("pat@example.com");
  assertThat(captor.getValue().getRole()).isEqualTo("PATIENT");
}




  @Test
  void login_shouldReturn200_withUserResponse() throws Exception {
    UserLoginDTO login = new UserLoginDTO();
    trySet(login, "email", "admin@example.com");
    trySet(login, "password", "adminpass");

    UserResponseDTO resp = new UserResponseDTO();
    // set fields your controller returns; adapt to your DTO:
    // e.g. resp.setToken("jwt-token"); resp.setEmail("admin@example.com"); resp.setRole("ADMIN");
    trySet(resp, "token", "jwt-token");
    trySet(resp, "email", "admin@example.com");
    trySet(resp, "role", "ADMIN");

    when(authService.login(any(UserLoginDTO.class))).thenReturn(resp);

    mvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(login)))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.token").value("jwt-token"))
        .andExpect(jsonPath("$.email").value("admin@example.com"))
        .andExpect(jsonPath("$.role").value("ADMIN"));

    verify(authService, times(1)).login(any(UserLoginDTO.class));
  }

@Test
void register_whenServiceThrows_shouldReturn500_fromExceptionHandler() throws Exception {
  RegisterRequestDTO req = new RegisterRequestDTO();
  req.setFirstName("Pat");
  req.setLastName("Patient");
  req.setEmail("bad@example.com");
  req.setPassword("secret123");
  req.setRole("PATIENT"); // or enum constant if the field is an enum

  doThrow(new RuntimeException("boom"))
      .when(authService)
      .registerPatient(any());

  mvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .characterEncoding("utf-8")
          .content(objectMapper.writeValueAsString(req))
          // if Spring Security filters are ON, add CSRF:
          //.with(csrf())
      )
      .andExpect(status().isInternalServerError())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.error").value("Something went wrong"));
}


  @Test
  void login_whenInvalidPayload_shouldReturn400() throws Exception {
    // Missing required fields to trigger @Valid (adjust to your DTO constraints)
    String invalidJson = "{}";

    mvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
        .andDo(print()) // <---- prints the request/response in the test log
        .andExpect(status().isBadRequest());

  }

  // ---------- tiny reflection helpers so this compiles even if your DTOs use Lombok and no builder ----------
  private static void trySet(Object target, String field, Object value) {
    try {
      var m = target.getClass().getMethod("set" + capitalize(field), value.getClass());
      m.invoke(target, value);
    } catch (Exception ignored) { /* if field not present, ignore so you can adapt later */ }
  }
  private static Object read(Object target, String field) {
    try {
      var m = target.getClass().getMethod("get" + capitalize(field));
      return m.invoke(target);
    } catch (Exception e) { return null; }
  }
  private static String capitalize(String s) { return s.substring(0,1).toUpperCase() + s.substring(1); }
}
