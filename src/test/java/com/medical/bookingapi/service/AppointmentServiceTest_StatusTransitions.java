package com.medical.bookingapi.service;

import com.medical.bookingapi.dto.AppointmentDTO;
import com.medical.bookingapi.mapper.AppointmentMapper;
import com.medical.bookingapi.model.Appointment;
import com.medical.bookingapi.repository.AppointmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentServiceTest_StatusTransitions {

  @Mock AppointmentRepository appointmentRepository;
  @Mock AppointmentMapper appointmentMapper;
  @InjectMocks AppointmentServiceImpl service;

  @BeforeEach
  void init() {
    MockitoAnnotations.openMocks(this);
  }

  private Appointment appointmentWithStatus(Long id, String status) {
    Appointment a = new Appointment();
    a.setId(id);
    a.setStatus(status);
    return a;
  }

  @Test
  void updateStatus_pendingToConfirmed_ok() {
    Appointment a = appointmentWithStatus(1L, "PENDING");
    when(appointmentRepository.findById(1L)).thenReturn(Optional.of(a));
    when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));
    AppointmentDTO out = new AppointmentDTO();
    when(appointmentMapper.toDto(any(Appointment.class))).thenReturn(out);

    AppointmentDTO result = service.updateStatus(1L, "CONFIRMED");
    assertSame(out, result);

    ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
    verify(appointmentRepository).save(captor.capture());
    assertEquals("CONFIRMED", captor.getValue().getStatus());
  }

  @Test
  void updateStatus_pendingToCancelled_ok() {
    Appointment a = appointmentWithStatus(2L, "PENDING");
    when(appointmentRepository.findById(2L)).thenReturn(Optional.of(a));
    when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));
    when(appointmentMapper.toDto(any(Appointment.class))).thenReturn(new AppointmentDTO());

    AppointmentDTO result = service.updateStatus(2L, "cancelled"); // case-insensitive allowed?
    assertNotNull(result);

    ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
    verify(appointmentRepository).save(captor.capture());
    assertEquals("CANCELLED", captor.getValue().getStatus());
  }

  @Test
  void updateStatus_confirmedToCancelled_ok() {
    Appointment a = appointmentWithStatus(3L, "CONFIRMED");
    when(appointmentRepository.findById(3L)).thenReturn(Optional.of(a));
    when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));
    when(appointmentMapper.toDto(any(Appointment.class))).thenReturn(new AppointmentDTO());

    service.updateStatus(3L, "CANCELLED");

    ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
    verify(appointmentRepository).save(captor.capture());
    assertEquals("CANCELLED", captor.getValue().getStatus());
  }

  @Test
  void updateStatus_confirmedToPending_rejected() {
    Appointment a = appointmentWithStatus(4L, "CONFIRMED");
    when(appointmentRepository.findById(4L)).thenReturn(Optional.of(a));

    IllegalStateException ex =
        assertThrows(IllegalStateException.class, () -> service.updateStatus(4L, "PENDING"));
    assertTrue(ex.getMessage().contains("Invalid status transition"));

    verify(appointmentRepository, never()).save(any());
  }

  @Test
  void updateStatus_cancelledToAnything_rejected() {
    Appointment a = appointmentWithStatus(5L, "CANCELLED");
    when(appointmentRepository.findById(5L)).thenReturn(Optional.of(a));

    IllegalStateException ex =
        assertThrows(IllegalStateException.class, () -> service.updateStatus(5L, "CONFIRMED"));
    assertTrue(ex.getMessage().contains("Invalid status transition"));

    verify(appointmentRepository, never()).save(any());
  }

  @Test
  void updateStatus_whenAppointmentMissing_throws404ish() {
    when(appointmentRepository.findById(404L)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> service.updateStatus(404L, "CONFIRMED"));

    verify(appointmentRepository, never()).save(any());
    verify(appointmentMapper, never()).toDto(any());
  }
}
