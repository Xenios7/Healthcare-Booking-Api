package com.medical.bookingapi.service;

import com.medical.bookingapi.dto.UserResponseDTO;
import com.medical.bookingapi.mapper.UserMapper;
import com.medical.bookingapi.model.Patient; // concrete subclass of User
import com.medical.bookingapi.model.User;
import com.medical.bookingapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock UserRepository userRepository;
  @Mock UserMapper userMapper;

  @InjectMocks UserServiceImpl service;

  @Test
  void getUserByEmail_updatesLastLogin_saves_andReturnsDto() {
    String email = "user@example.com";

    Patient user = new Patient();
    user.setId(1L);
    user.setEmail(email);
    user.setFirstName("Pat");
    user.setLastName("Ent");
    user.setRole("PATIENT");

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    UserResponseDTO mapped = new UserResponseDTO(); // adapt if yours needs fields
    when(userMapper.toDto(user)).thenReturn(mapped);

    LocalDateTime before = LocalDateTime.now().minusSeconds(2);

    UserResponseDTO result = service.getUserByEmail(email);

    LocalDateTime after  = LocalDateTime.now().plusSeconds(2);

    assertSame(mapped, result);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    User saved = captor.getValue();

    assertNotNull(saved.getLastLogin(), "lastLogin should be set");
    assertTrue(!saved.getLastLogin().isBefore(before) && !saved.getLastLogin().isAfter(after),
        "lastLogin should be within a few seconds of now");

    verify(userRepository).findByEmail(email);
    verify(userMapper).toDto(user);
  }

  @Test
  void getUserByEmail_throws_whenMissing() {
    when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

    UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
        () -> service.getUserByEmail("missing@example.com"));

    assertEquals("User not found", ex.getMessage());
    verify(userRepository, never()).save(any());
    verify(userMapper, never()).toDto(any());
  }

  @Test
  void updateLastLogin_updatesExistingUser_andSaves() {

    Patient incoming = new Patient();
    incoming.setId(42L);

    Patient existing = new Patient();
    existing.setId(42L);
    existing.setEmail("who@ever.com");

    when(userRepository.findById(42L)).thenReturn(Optional.of(existing));
    when(userRepository.save(existing)).thenReturn(existing);

    LocalDateTime before = LocalDateTime.now().minusSeconds(2);

    service.updateLastLogin(incoming);

    LocalDateTime after = LocalDateTime.now().plusSeconds(2);

    verify(userRepository).findById(42L);
    verify(userRepository).save(existing);

    assertNotNull(existing.getLastLogin());
    assertTrue(!existing.getLastLogin().isBefore(before) && !existing.getLastLogin().isAfter(after),
        "lastLogin should be within a few seconds of now");
  }

  @Test
  void updateLastLogin_throws_whenUserMissing() {
    Patient incoming = new Patient();
    incoming.setId(999L);

    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
        () -> service.updateLastLogin(incoming));

    assertEquals("User not found", ex.getMessage());
    verify(userRepository, never()).save(any());
  }
}
