package com.medical.bookingapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.bookingapi.dto.UserResponseDTO;
import com.medical.bookingapi.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = UserController.class,
    excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE,
                             classes = com.medical.bookingapi.security.JwtFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;

  @MockBean UserService userService;

  @Test
  void login_shouldReturn200_andBody() throws Exception {
    // Arrange
    String email = "user@example.com";
    String json = """
      {"email":"%s","password":"secret123"}
    """.formatted(email);

    // If UserResponseDTO doesn't have a no-args ctor, mocking is safe:
    UserResponseDTO response = org.mockito.Mockito.mock(UserResponseDTO.class);
    when(userService.getUserByEmail(eq(email))).thenReturn(response);

    // Act & Assert
    mvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

    verify(userService).getUserByEmail(email);
  }

  @Test
  void login_withInvalidPayload_shouldReturn400() throws Exception {
    // Missing/blank fields should fail @Valid on UserLoginDTO
    String badJson = """
      {"email":"","password":""}
    """;

    mvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(badJson))
        .andExpect(status().isBadRequest());
  }
}
