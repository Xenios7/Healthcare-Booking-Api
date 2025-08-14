package com.medical.bookingapi.service;

import com.medical.bookingapi.model.Patient;     // ← concrete subclass of User
import com.medical.bookingapi.repository.UserRepository;
import com.medical.bookingapi.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

  @Mock UserRepository userRepository;
  @InjectMocks CustomUserDetailsService service;

  @Test
  void loadUserByUsername_returnsCustomUserDetails_whenUserExists() {
    String email = "jane@example.com";

    // Use a concrete subclass of User
    Patient u = new Patient();
    u.setId(1L);
    u.setFirstName("Jane");
    u.setLastName("Doe");
    u.setEmail(email);
    u.setPassword_hash("hashedPw");
    u.setRole("PATIENT");

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(u));

    UserDetails details = service.loadUserByUsername(email);

    assertNotNull(details);
    assertTrue(details instanceof CustomUserDetails);
    assertEquals(email, details.getUsername());
    assertEquals("hashedPw", details.getPassword());

    CustomUserDetails cud = (CustomUserDetails) details;
    assertSame(u, cud.getUser());
    assertFalse(details.getAuthorities().isEmpty());
    // If your CustomUserDetails maps roles as ROLE_*
    // assertTrue(details.getAuthorities().stream()
    //     .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT")));

    verify(userRepository).findByEmail(email);
  }

  @Test
  void loadUserByUsername_throws_whenUserMissing() {
    String email = "missing@example.com";
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    UsernameNotFoundException ex = assertThrows(
        UsernameNotFoundException.class,
        () -> service.loadUserByUsername(email)
    );

    assertTrue(ex.getMessage().contains(email));
    verify(userRepository).findByEmail(email);
  }
}
