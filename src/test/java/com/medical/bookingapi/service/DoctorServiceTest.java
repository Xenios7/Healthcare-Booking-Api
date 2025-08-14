package com.medical.bookingapi.service;

import com.medical.bookingapi.dto.DoctorDTO;
import com.medical.bookingapi.dto.DoctorProfileUpdateDTO;
import com.medical.bookingapi.dto.DoctorRegistrationDTO;
import com.medical.bookingapi.mapper.DoctorMapper;
import com.medical.bookingapi.model.Doctor;
import com.medical.bookingapi.repository.DoctorRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

  @Mock DoctorRepository doctorRepository;
  @Mock DoctorMapper doctorMapper;
  @Mock PasswordEncoder passwordEncoder;

  @InjectMocks DoctorServiceImpl service;

  Doctor doctor;
  DoctorDTO doctorDto;

  @BeforeEach
  void setUp() {
    doctor = new Doctor();
    doctor.setId(10L);
    doctor.setFirstName("Gregory");
    doctor.setLastName("House");
    doctor.setEmail("house@ppth.com");
    doctor.setSpeciality("Diagnostics");
    doctor.setLocation("Princeton-Plainsboro");
    doctor.setLicenseNumber("NJ-0001");
    doctor.setRole("DOCTOR");

    doctorDto = new DoctorDTO(); // adapt if your DTO needs specific fields
  }

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  // ---------- findById ----------

  @Test
  void findById_returnsMappedDoctor() {
    when(doctorRepository.findById(10L)).thenReturn(Optional.of(doctor));
    when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);

    DoctorDTO result = service.findById(10L);

    assertSame(doctorDto, result);
    verify(doctorRepository).findById(10L);
    verify(doctorMapper).toDto(doctor);
  }

  @Test
  void findById_whenMissing_throws() {
    when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

    UsernameNotFoundException ex =
        assertThrows(UsernameNotFoundException.class, () -> service.findById(99L));
    assertEquals("User not found", ex.getMessage());
  }

  // ---------- findByEmail ----------

  @Test
  void findByEmail_returnsMappedDoctor() {
    when(doctorRepository.findByEmail("house@ppth.com")).thenReturn(Optional.of(doctor));
    when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);

    DoctorDTO result = service.findByEmail("house@ppth.com");

    assertSame(doctorDto, result);
    verify(doctorRepository).findByEmail("house@ppth.com");
    verify(doctorMapper).toDto(doctor);
  }

  @Test
  void findByEmail_whenMissing_throws() {
    when(doctorRepository.findByEmail("missing@x.com")).thenReturn(Optional.empty());

    UsernameNotFoundException ex =
        assertThrows(UsernameNotFoundException.class, () -> service.findByEmail("missing@x.com"));
    assertEquals("User not found", ex.getMessage());
  }

  // ---------- findBySpeciality ----------

  @Test
  void findBySpeciality_returnsMappedList() {
    when(doctorRepository.findBySpeciality("Diagnostics")).thenReturn(List.of(doctor));
    when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);

    var results = service.findBySpeciality("Diagnostics");

    assertEquals(1, results.size());
    assertSame(doctorDto, results.get(0));
    verify(doctorRepository).findBySpeciality("Diagnostics");
  }

  @Test
  void findBySpeciality_whenEmpty_throws() {
    when(doctorRepository.findBySpeciality("Neuro")).thenReturn(List.of());

    UsernameNotFoundException ex =
        assertThrows(UsernameNotFoundException.class, () -> service.findBySpeciality("Neuro"));
    assertEquals("No doctors found", ex.getMessage());
  }

  // ---------- createDoctor ----------

  @Test
  void createDoctor_hashesPassword_setsRole_saves_andReturnsDto() {
    DoctorRegistrationDTO reg = new DoctorRegistrationDTO();
    // If your DTO has setters, set only what’s needed:
    trySet(reg, "firstName", "Gregory");
    trySet(reg, "lastName", "House");
    trySet(reg, "email", "house@ppth.com");
    trySet(reg, "password", "plain");
    trySet(reg, "speciality", "Diagnostics");
    trySet(reg, "location", "Princeton-Plainsboro");
    trySet(reg, "licenseNumber", "NJ-0001");

    Doctor mappedEntity = new Doctor(); // entity created by mapper from DTO
    when(doctorMapper.toEntity(reg)).thenReturn(mappedEntity);
    when(passwordEncoder.encode("plain")).thenReturn("hashed");
    when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));
    when(doctorMapper.toDto(any(Doctor.class))).thenReturn(doctorDto);

    DoctorDTO result = service.createDoctor(reg);

    assertSame(doctorDto, result);
    ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);
    verify(doctorRepository).save(captor.capture());
    Doctor saved = captor.getValue();

    assertEquals("hashed", saved.getPassword_hash());
    assertEquals("DOCTOR", saved.getRole());
    verify(passwordEncoder).encode("plain");
    verify(doctorMapper).toEntity(reg);
    verify(doctorMapper).toDto(saved);
  }

  // ---------- me() ----------

  @Test
  void me_returnsCurrentDoctorDto_fromSecurityContextEmail() {
    // put a principal email into the SecurityContext
    var auth = new UsernamePasswordAuthenticationToken("house@ppth.com", "N/A");
    SecurityContextHolder.getContext().setAuthentication(auth);

    when(doctorRepository.findByEmail("house@ppth.com")).thenReturn(Optional.of(doctor));
    when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);

    DoctorDTO result = service.me();

    assertSame(doctorDto, result);
    verify(doctorRepository).findByEmail("house@ppth.com");
  }

  @Test
  void me_whenNotFound_throws() {
    var auth = new UsernamePasswordAuthenticationToken("missing@x.com", "N/A");
    SecurityContextHolder.getContext().setAuthentication(auth);
    when(doctorRepository.findByEmail("missing@x.com")).thenReturn(Optional.empty());

    UsernameNotFoundException ex =
        assertThrows(UsernameNotFoundException.class, () -> service.me());
    assertEquals("Doctor not found", ex.getMessage());
  }

  // ---------- updateMyProfile ----------

  @Test
  void updateMyProfile_appliesFields_saves_andReturnsDto() {
    var auth = new UsernamePasswordAuthenticationToken("house@ppth.com", "N/A");
    SecurityContextHolder.getContext().setAuthentication(auth);

    when(doctorRepository.findByEmail("house@ppth.com")).thenReturn(Optional.of(doctor));
    when(doctorRepository.save(doctor)).thenReturn(doctor);
    when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);

    DoctorProfileUpdateDTO patch = new DoctorProfileUpdateDTO();
    trySet(patch, "firstName", "Greg");
    trySet(patch, "lastName", "H.");
    trySet(patch, "speciality", "Nephrology");
    trySet(patch, "location", "New Clinic");
    trySet(patch, "licenseNumber", "NJ-777");

    DoctorDTO result = service.updateMyProfile(patch);

    assertSame(doctorDto, result);
    assertEquals("Greg", doctor.getFirstName());
    assertEquals("H.", doctor.getLastName());
    assertEquals("Nephrology", doctor.getSpeciality());
    assertEquals("New Clinic", doctor.getLocation());
    assertEquals("NJ-777", doctor.getLicenseNumber());
    verify(doctorRepository).save(doctor);
  }

  @Test
  void updateMyProfile_whenNotFound_throws() {
    var auth = new UsernamePasswordAuthenticationToken("missing@x.com", "N/A");
    SecurityContextHolder.getContext().setAuthentication(auth);
    when(doctorRepository.findByEmail("missing@x.com")).thenReturn(Optional.empty());

    UsernameNotFoundException ex =
        assertThrows(UsernameNotFoundException.class, () -> service.updateMyProfile(new DoctorProfileUpdateDTO()));
    assertEquals("Doctor not found", ex.getMessage());
    verify(doctorRepository, never()).save(any());
  }

  // --- tiny reflection helper so tests compile even if DTOs are Lombok-only ---
  private static void trySet(Object target, String field, Object value) {
    try {
      var m = target.getClass().getMethod("set" + Character.toUpperCase(field.charAt(0)) + field.substring(1), value.getClass());
      m.invoke(target, value);
    } catch (Exception ignored) { }
  }
}
