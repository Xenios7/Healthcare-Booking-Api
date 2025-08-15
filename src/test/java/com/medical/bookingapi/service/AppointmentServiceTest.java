package com.medical.bookingapi.service;

import com.medical.bookingapi.dto.AppointmentCreateDTO;
import com.medical.bookingapi.dto.AppointmentDTO;
import com.medical.bookingapi.mapper.AppointmentMapper;
import com.medical.bookingapi.model.Appointment;
import com.medical.bookingapi.model.AppointmentSlot;
import com.medical.bookingapi.model.Doctor;
import com.medical.bookingapi.model.Patient;
import com.medical.bookingapi.repository.AppointmentRepository;
import com.medical.bookingapi.repository.AppointmentSlotRepository;
import com.medical.bookingapi.repository.DoctorRepository;
import com.medical.bookingapi.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

  @Mock DoctorRepository doctorRepository;
  @Mock PatientRepository patientRepository;
  @Mock AppointmentSlotRepository slotRepository;
  @Mock AppointmentRepository appointmentRepository;
  @Mock AppointmentMapper appointmentMapper;

  @InjectMocks AppointmentServiceImpl service;

  Doctor doctor;
  Patient patient;
  AppointmentSlot slot;
  Appointment appt;
  AppointmentDTO dtoMapped;

  @BeforeEach
  void setUp() {
    doctor = new Doctor();
    doctor.setId(7L);
    doctor.setSpeciality("Cardiology");
    doctor.setLocation("Nicosia");

    patient = new Patient();
    patient.setId(3L);

    slot = new AppointmentSlot();
    slot.setId(100L);
    slot.setStartTime(LocalDateTime.of(2025, 1, 20, 10, 0));
    slot.setEndTime(LocalDateTime.of(2025, 1, 20, 10, 30));

    appt = new Appointment();
    appt.setId(200L);
    appt.setDoctor(doctor);
    appt.setPatient(patient);
    appt.setSlot(slot);
    appt.setStatus("BOOKED");

    dtoMapped = new AppointmentDTO(); 
  }

  // ---------- Reads ----------

  @Test
  void findByDoctorId_returnsMappedList() {
    when(doctorRepository.findById(7L)).thenReturn(Optional.of(doctor));
    when(appointmentRepository.findByDoctor(doctor)).thenReturn(List.of(appt));
    when(appointmentMapper.toDto(appt)).thenReturn(dtoMapped);

    var result = service.findByDoctorId(7L);

    assertEquals(1, result.size());
    assertSame(dtoMapped, result.get(0));
    verify(doctorRepository).findById(7L);
    verify(appointmentRepository).findByDoctor(doctor);
    verify(appointmentMapper).toDto(appt);
  }

  @Test
  void findByDoctorId_throwsWhenDoctorMissing() {
    when(doctorRepository.findById(9L)).thenReturn(Optional.empty());
    var ex = assertThrows(EntityNotFoundException.class, () -> service.findByDoctorId(9L));
    assertEquals("Doctor not found", ex.getMessage());
  }

  @Test
  void findByPatientId_returnsMappedList() {
    when(patientRepository.findById(3L)).thenReturn(Optional.of(patient));
    when(appointmentRepository.findByPatient(patient)).thenReturn(List.of(appt));
    when(appointmentMapper.toDto(appt)).thenReturn(dtoMapped);

    var result = service.findByPatientId(3L);

    assertEquals(1, result.size());
    verify(patientRepository).findById(3L);
    verify(appointmentRepository).findByPatient(patient);
  }

  @Test
  void findBySlotId_returnsOptionalMapped() {
    when(slotRepository.findById(100L)).thenReturn(Optional.of(slot));
    when(appointmentRepository.findBySlot(slot)).thenReturn(Optional.of(appt));
    when(appointmentMapper.toDto(appt)).thenReturn(dtoMapped);

    var result = service.findBySlotId(100L);

    assertTrue(result.isPresent());
    assertSame(dtoMapped, result.get());
    verify(slotRepository).findById(100L);
    verify(appointmentRepository).findBySlot(slot);
  }

  @Test
  void findBySlotId_throwsWhenSlotMissing() {
    when(slotRepository.findById(111L)).thenReturn(Optional.empty());
    var ex = assertThrows(EntityNotFoundException.class, () -> service.findBySlotId(111L));
    assertEquals("Slot not found", ex.getMessage());
  }

  @Test
  void findByStatus_returnsMappedList() {
    when(appointmentRepository.findByStatus("BOOKED")).thenReturn(List.of(appt));
    when(appointmentMapper.toDto(appt)).thenReturn(dtoMapped);

    var result = service.findByStatus("BOOKED");

    assertEquals(1, result.size());
    verify(appointmentRepository).findByStatus("BOOKED");
  }

  @Test
  void existsBySlot_delegatesToRepo() {
    when(appointmentRepository.existsBySlot(slot)).thenReturn(true);
    assertTrue(service.existsBySlot(slot));
    verify(appointmentRepository).existsBySlot(slot);
  }

  @Test
  void findByDoctorAndStatus_returnsMappedList() {
    when(doctorRepository.findById(7L)).thenReturn(Optional.of(doctor));
    when(appointmentRepository.findByDoctorAndStatus(doctor, "BOOKED")).thenReturn(List.of(appt));
    when(appointmentMapper.toDto(appt)).thenReturn(dtoMapped);

    var result = service.findByDoctorAndStatus(7L, "BOOKED");

    assertEquals(1, result.size());
    verify(doctorRepository).findById(7L);
    verify(appointmentRepository).findByDoctorAndStatus(doctor, "BOOKED");
  }

  // ---------- Create (book) ----------

  @Test
  void bookAppointment_mapsEntity_setsRelations_andSaves() {
    AppointmentCreateDTO create = new AppointmentCreateDTO();
    trySet(create, "doctorId", 7L);
    trySet(create, "patientId", 3L);
    trySet(create, "slotId", 100L);

    when(appointmentMapper.toEntity(create)).thenReturn(new Appointment());
    when(doctorRepository.findById(7L)).thenReturn(Optional.of(doctor));
    when(patientRepository.findById(3L)).thenReturn(Optional.of(patient));
    when(slotRepository.findById(100L)).thenReturn(Optional.of(slot));
    when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
      Appointment saved = inv.getArgument(0);
      saved.setId(200L);
      return saved;
    });
    when(appointmentMapper.toDto(any(Appointment.class))).thenReturn(dtoMapped);

    // Act
    var result = service.bookAppointment(create);

    // Assert
    assertNotNull(result);
    ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
    verify(appointmentRepository).save(captor.capture());
    Appointment saved = captor.getValue();

    assertSame(doctor, saved.getDoctor());
    assertSame(patient, saved.getPatient());
    assertSame(slot, saved.getSlot());
    assertEquals("BOOKED", saved.getStatus());

    verify(appointmentMapper).toEntity(create);
    verify(appointmentMapper).toDto(saved);
  }

  @Test
  void bookAppointment_throwsWhenDoctorMissing() {
    AppointmentCreateDTO create = new AppointmentCreateDTO();
    trySet(create, "doctorId", 99L);
    trySet(create, "patientId", 3L);
    trySet(create, "slotId", 100L);

    when(appointmentMapper.toEntity(create)).thenReturn(new Appointment());
    when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

    var ex = assertThrows(EntityNotFoundException.class, () -> service.bookAppointment(create));
    assertEquals("Doctor not found", ex.getMessage());
    verify(appointmentRepository, never()).save(any());
  }

  @Test
  void bookAppointment_throwsWhenPatientMissing() {
    AppointmentCreateDTO create = new AppointmentCreateDTO();
    trySet(create, "doctorId", 7L);
    trySet(create, "patientId", 33L);
    trySet(create, "slotId", 100L);

    when(appointmentMapper.toEntity(create)).thenReturn(new Appointment());
    when(doctorRepository.findById(7L)).thenReturn(Optional.of(doctor));
    when(patientRepository.findById(33L)).thenReturn(Optional.empty());

    var ex = assertThrows(EntityNotFoundException.class, () -> service.bookAppointment(create));
    assertEquals("Patient not found", ex.getMessage());
    verify(appointmentRepository, never()).save(any());
  }

  @Test
  void bookAppointment_throwsWhenSlotMissing() {
    AppointmentCreateDTO create = new AppointmentCreateDTO();
    trySet(create, "doctorId", 7L);
    trySet(create, "patientId", 3L);
    trySet(create, "slotId", 101L);

    when(appointmentMapper.toEntity(create)).thenReturn(new Appointment());
    when(doctorRepository.findById(7L)).thenReturn(Optional.of(doctor));
    when(patientRepository.findById(3L)).thenReturn(Optional.of(patient));
    when(slotRepository.findById(101L)).thenReturn(Optional.empty());

    var ex = assertThrows(EntityNotFoundException.class, () -> service.bookAppointment(create));
    assertEquals("Slot not found", ex.getMessage());
    verify(appointmentRepository, never()).save(any());
  }

  // ---------- Update status ----------

  @Test
  void updateStatus_updatesAndSaves() {
    when(appointmentRepository.findById(200L)).thenReturn(Optional.of(appt));
    when(appointmentRepository.save(appt)).thenReturn(appt);
    when(appointmentMapper.toDto(appt)).thenReturn(dtoMapped);

    var result = service.updateStatus(200L, "CANCELLED");

    assertSame(dtoMapped, result);
    assertEquals("CANCELLED", appt.getStatus());
    verify(appointmentRepository).findById(200L);
    verify(appointmentRepository).save(appt);
    verify(appointmentMapper).toDto(appt);
  }
  
  @Test
  void updateStatus_throwsWhenMissing() {
      when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());
      var ex = assertThrows(EntityNotFoundException.class,
              () -> service.updateStatus(999L, "CANCELLED"));
      assertEquals("Appointment not found with ID: 999", ex.getMessage());
  }


  // ---------- Delete ----------

  @Test
  void deleteAppointment_deletesWhenExists() {
    when(appointmentRepository.existsById(200L)).thenReturn(true);

    service.deleteAppointment(200L);

    verify(appointmentRepository).existsById(200L);
    verify(appointmentRepository).deleteById(200L);
  }

  @Test
  void deleteAppointment_throwsWhenMissing() {
    when(appointmentRepository.existsById(404L)).thenReturn(false);

    var ex = assertThrows(EntityNotFoundException.class, () -> service.deleteAppointment(404L));
    assertTrue(ex.getMessage().contains("404"));
    verify(appointmentRepository, never()).deleteById(anyLong());
  }

  // helper: tolerate DTOs without setters/builders
  private static void trySet(Object target, String field, Object value) {
    try {
      var m = target.getClass().getMethod("set" + Character.toUpperCase(field.charAt(0)) + field.substring(1), value.getClass());
      m.invoke(target, value);
    } catch (Exception ignored) {}
  }
}
