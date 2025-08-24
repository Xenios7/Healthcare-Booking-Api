package com.medical.bookingapi.service;

import com.medical.bookingapi.dto.PatientDTO;
import com.medical.bookingapi.dto.PatientProfileUpdateDTO;
import com.medical.bookingapi.dto.PatientRegistrationDTO;
import com.medical.bookingapi.mapper.PatientMapper;
import com.medical.bookingapi.model.Patient;
import com.medical.bookingapi.repository.PatientRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

  @Mock PatientRepository patientRepository;
  @Mock PatientMapper patientMapper;
  @Mock PasswordEncoder passwordEncoder;

  @InjectMocks PatientServiceImpl service;

  Patient patient;
  PatientDTO patientDto;

  @BeforeEach
  void setUp() {
    patient = new Patient();
    patient.setId(5L);
    patient.setFirstName("Jane");
    patient.setLastName("Doe");
    patient.setEmail("jane@example.com");
    patient.setBloodType("O+");
    patient.setAllergies("peanuts");
    patient.setInsuranceId("INS-123");
    patient.setDateOfBirth(LocalDate.of(1995, 5, 5));
    patient.setRole("PATIENT");

    patientDto = new PatientDTO(); 
  }

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  // -------- getPatientById --------

  @Test
  void getPatientById_returnsDto_whenFound() {
    when(patientRepository.findById(5L)).thenReturn(Optional.of(patient));
    when(patientMapper.toDto(patient)).thenReturn(patientDto);

    PatientDTO result = service.getPatientById(5L);

    assertSame(patientDto, result);
    verify(patientRepository).findById(5L);
    verify(patientMapper).toDto(patient);
  }

  @Test
  void getPatientById_throws_whenMissing() {
    when(patientRepository.findById(404L)).thenReturn(Optional.empty());

    UsernameNotFoundException ex =
        assertThrows(UsernameNotFoundException.class, () -> service.getPatientById(404L));
    assertEquals("Patient not found", ex.getMessage());
  }

  // -------- findByEmail --------

  @Test
  void findByEmail_mapsToDto_whenPresent() {
    when(patientRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(patient));
    when(patientMapper.toDto(patient)).thenReturn(patientDto);

    var opt = service.findByEmail("jane@example.com");

    assertTrue(opt.isPresent());
    assertSame(patientDto, opt.get());
  }

  @Test
  void findByEmail_empty_whenMissing() {
    when(patientRepository.findByEmail("none@x.com")).thenReturn(Optional.empty());

    var opt = service.findByEmail("none@x.com");

    assertTrue(opt.isEmpty());
    verify(patientMapper, never()).toDto(any());
  }

  // -------- findByBloodType --------

  @Test
  void findByBloodType_returnsMappedList() {
    when(patientRepository.findByBloodType("O+")).thenReturn(List.of(patient));
    when(patientMapper.toDto(patient)).thenReturn(patientDto);

    var results = service.findByBloodType("O+");

    assertEquals(1, results.size());
    assertSame(patientDto, results.get(0));
    verify(patientRepository).findByBloodType("O+");
  }

  // -------- createPatient --------

  @Test
  void createPatient_mapsEntity_hashesPassword_setsRole_saves_andReturnsDto() {
    PatientRegistrationDTO reg = new PatientRegistrationDTO();
    trySet(reg, "firstName", "Jane");
    trySet(reg, "lastName", "Doe");
    trySet(reg, "email", "jane@example.com");
    trySet(reg, "password", "plain");
    trySet(reg, "bloodType", "O+");
    trySet(reg, "allergies", "peanuts");
    trySet(reg, "insuranceId", "INS-123");
    trySet(reg, "dateOfBirth", LocalDate.of(1995,5,5));

    Patient mappedEntity = new Patient(); 
    when(patientMapper.toEntity(reg)).thenReturn(mappedEntity);
    when(passwordEncoder.encode("plain")).thenReturn("hashed");
    when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));
    when(patientMapper.toDto(any(Patient.class))).thenReturn(patientDto);

    PatientDTO result = service.createPatient(reg);

    assertSame(patientDto, result);
    ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
    verify(patientRepository).save(captor.capture());
    Patient saved = captor.getValue();

    assertEquals("hashed", saved.getPassword_hash());
    assertEquals("PATIENT", saved.getRole());
    verify(passwordEncoder).encode("plain");
    verify(patientMapper).toEntity(reg);
    verify(patientMapper).toDto(saved);
  }

  // -------- me() --------

  @Test
  void me_returnsCurrentPatientDto_fromSecurityContextEmail() {
    var auth = new UsernamePasswordAuthenticationToken("jane@example.com", "N/A");
    SecurityContextHolder.getContext().setAuthentication(auth);

    when(patientRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(patient));
    when(patientMapper.toDto(patient)).thenReturn(patientDto);

    PatientDTO result = service.me();

    assertSame(patientDto, result);
    verify(patientRepository).findByEmail("jane@example.com");
  }

  @Test
  void me_whenNotFound_throws() {
    var auth = new UsernamePasswordAuthenticationToken("missing@x.com", "N/A");
    SecurityContextHolder.getContext().setAuthentication(auth);

    when(patientRepository.findByEmail("missing@x.com")).thenReturn(Optional.empty());

    UsernameNotFoundException ex =
        assertThrows(UsernameNotFoundException.class, () -> service.me());
    assertEquals("Doctor not found", ex.getMessage()); 
  }

  // -------- updateMyProfile --------

  @Test
  void updateMyProfile_appliesFields_saves_andReturnsDto() {
    var auth = new UsernamePasswordAuthenticationToken("jane@example.com", "N/A");
    SecurityContextHolder.getContext().setAuthentication(auth);

    when(patientRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(patient));
    when(patientRepository.save(patient)).thenReturn(patient);
    when(patientMapper.toDto(patient)).thenReturn(patientDto);

    PatientProfileUpdateDTO patch = new PatientProfileUpdateDTO();
    trySet(patch, "dateOfBirth", LocalDate.of(1996, 6, 6));
    trySet(patch, "bloodType", "A+");
    trySet(patch, "allergies", "none");
    trySet(patch, "insuranceId", "INS-999");

    PatientDTO result = service.updateMyProfile(patch);

    assertSame(patientDto, result);
    assertEquals(LocalDate.of(1996,6,6), patient.getDateOfBirth());
    assertEquals("A+", patient.getBloodType());
    assertEquals("none", patient.getAllergies());
    assertEquals("INS-999", patient.getInsuranceId());
    verify(patientRepository).save(patient);
  }

  @Test
  void updateMyProfile_whenNotFound_throws() {
    var auth = new UsernamePasswordAuthenticationToken("missing@x.com", "N/A");
    SecurityContextHolder.getContext().setAuthentication(auth);

    when(patientRepository.findByEmail("missing@x.com")).thenReturn(Optional.empty());

    UsernameNotFoundException ex =
        assertThrows(UsernameNotFoundException.class, () -> service.updateMyProfile(new PatientProfileUpdateDTO()));
    assertEquals("Patient not found", ex.getMessage());
    verify(patientRepository, never()).save(any());
  }

  // ---- tiny helper so tests compile even if DTOs are Lombok-only ----
  private static void trySet(Object target, String field, Object value) {
    try {
      var m = target.getClass().getMethod(
          "set" + Character.toUpperCase(field.charAt(0)) + field.substring(1),
          value.getClass()
      );
      m.invoke(target, value);
    } catch (Exception ignored) { }
  }
}
