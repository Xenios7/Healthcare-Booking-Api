package com.medical.bookingapi.auth;

import com.medical.bookingapi.dto.*;
import com.medical.bookingapi.model.*;
import com.medical.bookingapi.repository.UserRepository;
import com.medical.bookingapi.security.CustomUserDetails;
import com.medical.bookingapi.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock private JwtService jwtService;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerPatient_savesPatient_whenEmailNotTaken() {
        RegisterRequestDTO dto = new RegisterRequestDTO("John", "Doe", "john@example.com", "pass123", null, null, null);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");

        authService.registerPatient(dto);

        verify(userRepository).save(argThat(user ->
            user instanceof Patient &&
            ((Patient) user).getFirstName().equals("John") &&
            ((Patient) user).getRole().equals("PATIENT") &&
            ((Patient) user).getPassword_hash().equals("encoded")
        ));
    }

    @Test
    void registerPatient_throws_whenEmailExists() {
        RegisterRequestDTO dto = new RegisterRequestDTO("John", "Doe", "john@example.com", "pass123", null, null, null);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.registerPatient(dto));

        assertEquals("Email already in use", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsUserResponse_whenValidCredentials() {
        UserLoginDTO loginDTO = new UserLoginDTO("john@example.com", "pass123");

        Patient patient = new Patient();
        patient.setId(1L);
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setEmail("john@example.com");
        patient.setRole("PATIENT");

        CustomUserDetails cud = new CustomUserDetails(patient);
        Authentication auth = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(cud);
        when(jwtService.generateToken(cud)).thenReturn("jwt-token");

        UserResponseDTO response = authService.login(loginDTO);

        assertEquals(1L, response.getId());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("PATIENT", response.getRole());
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void login_throws_whenInvalidCredentials() {
        UserLoginDTO loginDTO = new UserLoginDTO("john@example.com", "wrongpass");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(loginDTO));

        assertEquals("Invalid credentials", ex.getMessage());
    }
}
