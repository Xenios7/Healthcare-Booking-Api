package com.medical.bookingapi.service;

import com.medical.bookingapi.dto.AppointmentSlotDTO;
import com.medical.bookingapi.dto.SlotCreateDTO;
import com.medical.bookingapi.mapper.AppointmentSlotMapper;
import com.medical.bookingapi.model.AppointmentSlot;
import com.medical.bookingapi.model.Doctor;
import com.medical.bookingapi.repository.AppointmentSlotRepository;
import com.medical.bookingapi.repository.DoctorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AppointmentSlotServiceTest {

  @Mock AppointmentSlotRepository slotRepo;
  @Mock AppointmentSlotMapper slotMapper;
  @Mock DoctorRepository doctorRepo;

  @InjectMocks AppointmentSlotServiceImpl service;

  Doctor doctor;
  AppointmentSlot slot1;
  AppointmentSlot slot2;
  AppointmentSlotDTO dto1;

  @BeforeEach
  void setup() {
    doctor = new Doctor();
    doctor.setId(7L);
    doctor.setSpeciality("Cardiology");
    doctor.setLocation("Nicosia");

    slot1 = new AppointmentSlot();
    slot1.setId(100L);
    slot1.setDoctor(doctor);
    slot1.setStartTime(LocalDateTime.of(2025, 1, 20, 10, 0));
    slot1.setEndTime(LocalDateTime.of(2025, 1, 20, 10, 30));
    slot1.setBooked(false);
    slot1.setNotes("room A");

    slot2 = new AppointmentSlot();
    slot2.setId(101L);
    slot2.setDoctor(doctor);
    slot2.setStartTime(LocalDateTime.of(2025, 1, 20, 11, 0));
    slot2.setEndTime(LocalDateTime.of(2025, 1, 20, 11, 30));
    slot2.setBooked(true);

    dto1 = new AppointmentSlotDTO(); 
  }

  // ----------- findByDoctorId -----------

  @Test
  void findByDoctorId_returnsMappedList() {
    when(doctorRepo.findById(7L)).thenReturn(Optional.of(doctor));
    when(slotRepo.findByDoctor(doctor)).thenReturn(List.of(slot1, slot2));
    when(slotMapper.toDto(slot1)).thenReturn(dto1);
    when(slotMapper.toDto(slot2)).thenReturn(new AppointmentSlotDTO());

    var result = service.findByDoctorId(7L);

    assertEquals(2, result.size());
    verify(doctorRepo).findById(7L);
    verify(slotRepo).findByDoctor(doctor);
    verify(slotMapper, times(2)).toDto(any(AppointmentSlot.class));
  }

  @Test
  void findByDoctorId_throwsWhenDoctorMissing() {
    when(doctorRepo.findById(9L)).thenReturn(Optional.empty());

    var ex = assertThrows(EntityNotFoundException.class,
        () -> service.findByDoctorId(9L));
    assertTrue(ex.getMessage().contains("Doctor not found"));
  }

  // ----------- findByIsBookedFalse -----------

  @Test
  void findByIsBookedFalse_returnsMappedList() {
    when(slotRepo.findByBookedFalse()).thenReturn(List.of(slot1));
    when(slotMapper.toDto(slot1)).thenReturn(dto1);

    var result = service.findByIsBookedFalse();

    assertEquals(1, result.size());
    verify(slotRepo).findByBookedFalse();
    verify(slotMapper).toDto(slot1);
  }

  // ----------- findById -----------

  @Test
  void findById_returnsOptionalMapped() {
    when(slotRepo.findById(100L)).thenReturn(Optional.of(slot1));
    when(slotMapper.toDto(slot1)).thenReturn(dto1);

    var result = service.findById(100L);

    assertTrue(result.isPresent());
    assertSame(dto1, result.get());
    verify(slotRepo).findById(100L);
    verify(slotMapper).toDto(slot1);
  }

  @Test
  void findById_emptyWhenMissing() {
    when(slotRepo.findById(999L)).thenReturn(Optional.empty());

    var result = service.findById(999L);

    assertTrue(result.isEmpty());
    verify(slotRepo).findById(999L);
    verify(slotMapper, never()).toDto(any());
  }

  // ----------- findByDoctorAndIsBookedFalse -----------

  @Test
  void findByDoctorAndIsBookedFalse_returnsMappedList() {
    when(doctorRepo.findById(7L)).thenReturn(Optional.of(doctor));
    when(slotRepo.findByDoctorAndBookedFalse(doctor)).thenReturn(List.of(slot1));
    when(slotMapper.toDto(slot1)).thenReturn(dto1);

    var result = service.findByDoctorAndIsBookedFalse(7L);

    assertEquals(1, result.size());
    verify(doctorRepo).findById(7L);
    verify(slotRepo).findByDoctorAndBookedFalse(doctor);
  }

  @Test
  void findByDoctorAndIsBookedFalse_throwsWhenDoctorMissing() {
    when(doctorRepo.findById(7L)).thenReturn(Optional.empty());

    var ex = assertThrows(EntityNotFoundException.class,
        () -> service.findByDoctorAndIsBookedFalse(7L));
    assertTrue(ex.getMessage().contains("Doctor not found"));
  }

  // ----------- findByStartTimeBetween -----------

  @Test
  void findByStartTimeBetween_returnsMappedList() {
    var from = LocalDateTime.of(2025, 1, 20, 9, 0);
    var to   = LocalDateTime.of(2025, 1, 20, 12, 0);
    when(slotRepo.findByStartTimeBetween(from, to)).thenReturn(List.of(slot1, slot2));
    when(slotMapper.toDto(slot1)).thenReturn(dto1);
    when(slotMapper.toDto(slot2)).thenReturn(new AppointmentSlotDTO());

    var result = service.findByStartTimeBetween(from, to);

    assertEquals(2, result.size());
    verify(slotRepo).findByStartTimeBetween(from, to);
  }

  // ----------- findFirstByDoctorAndIsBookedFalseOrderByStartTimeAsc -----------

  @Test
  void findFirstFreeByDoctor_returnsMappedOptional() {
    when(doctorRepo.findById(7L)).thenReturn(Optional.of(doctor));
    when(slotRepo.findFirstByDoctorAndBookedFalseOrderByStartTimeAsc(doctor))
        .thenReturn(Optional.of(slot1));
    when(slotMapper.toDto(slot1)).thenReturn(dto1);

    var result = service.findFirstByDoctorAndIsBookedFalseOrderByStartTimeAsc(7L);

    assertTrue(result.isPresent());
    assertSame(dto1, result.get());
  }

  @Test
  void findFirstFreeByDoctor_throwsWhenDoctorMissing() {
    when(doctorRepo.findById(7L)).thenReturn(Optional.empty());

    var ex = assertThrows(EntityNotFoundException.class,
        () -> service.findFirstByDoctorAndIsBookedFalseOrderByStartTimeAsc(7L));
    assertTrue(ex.getMessage().contains("Doctor not found"));
  }

  @Test
  void findFirstFreeByDoctor_emptyWhenNoSlot() {
    when(doctorRepo.findById(7L)).thenReturn(Optional.of(doctor));
    when(slotRepo.findFirstByDoctorAndBookedFalseOrderByStartTimeAsc(doctor))
        .thenReturn(Optional.empty());

    var result = service.findFirstByDoctorAndIsBookedFalseOrderByStartTimeAsc(7L);

    assertTrue(result.isEmpty());
  }

  // ----------- createSlot -----------

  @Test
  void createSlot_mapsEntity_setsDoctor_unbooks_andSaves() {
    // Arrange
    var start = LocalDateTime.of(2025, 1, 22, 14, 0);
    var end   = LocalDateTime.of(2025, 1, 22, 14, 30);

    SlotCreateDTO create = new SlotCreateDTO();
    trySet(create, "doctorId", 7L);
    trySet(create, "startTime", start);
    trySet(create, "endTime",   end);
    trySet(create, "notes", "note");

    AppointmentSlot toPersist = new AppointmentSlot();
    toPersist.setStartTime(start);
    toPersist.setEndTime(end);
    when(slotMapper.toEntity(create)).thenReturn(toPersist);

    when(doctorRepo.findById(7L)).thenReturn(Optional.of(doctor));

    when(slotRepo.existsOverlapping(doctor, start, end)).thenReturn(false);

    when(slotRepo.save(any(AppointmentSlot.class))).thenAnswer(inv -> {
      AppointmentSlot s = inv.getArgument(0);
      s.setId(555L);
      return s;
    });

    AppointmentSlotDTO dtoOut = new AppointmentSlotDTO();
    when(slotMapper.toDto(any(AppointmentSlot.class))).thenReturn(dtoOut);

    // Act
    var result = service.createSlot(create);

    // Assert
    assertNotNull(result);
    ArgumentCaptor<AppointmentSlot> captor = ArgumentCaptor.forClass(AppointmentSlot.class);
    verify(slotRepo).save(captor.capture());
    AppointmentSlot saved = captor.getValue();

    assertSame(doctor, saved.getDoctor());
    assertEquals(start, saved.getStartTime());
    assertEquals(end, saved.getEndTime());
    assertFalse(saved.isBooked());

    verify(slotMapper).toEntity(create);
    verify(slotRepo).existsOverlapping(doctor, start, end);
    verify(slotMapper).toDto(saved);
  }


  @Test
  void createSlot_throwsWhenDoctorMissing() {
    SlotCreateDTO create = new SlotCreateDTO();
    trySet(create, "doctorId", 99L);

    when(slotMapper.toEntity(create)).thenReturn(new AppointmentSlot());
    when(doctorRepo.findById(99L)).thenReturn(Optional.empty());

    var ex = assertThrows(EntityNotFoundException.class, () -> service.createSlot(create));
    assertTrue(ex.getMessage().contains("Doctor not found"));
    verify(slotRepo, never()).save(any());
  }

  // ----------- updateSlot -----------

  @Test
  void updateSlot_happyPath_updatesFields_andSaves() {
    when(slotRepo.findById(100L)).thenReturn(Optional.of(slot1)); 
    when(slotRepo.save(slot1)).thenReturn(slot1);
    when(slotMapper.toDto(slot1)).thenReturn(dto1);

    AppointmentSlotDTO patch = new AppointmentSlotDTO();
    trySet(patch, "startTime", LocalDateTime.of(2025,1,21,9,0));
    trySet(patch, "endTime",   LocalDateTime.of(2025,1,21,9,30));
    trySet(patch, "booked", false);
    trySet(patch, "notes", "updated");

    var result = service.updateSlot(100L, patch);

    assertSame(dto1, result);
    assertEquals(LocalDateTime.of(2025,1,21,9,0), slot1.getStartTime());
    assertEquals(LocalDateTime.of(2025,1,21,9,30), slot1.getEndTime());
    assertFalse(slot1.isBooked());
    assertEquals("updated", slot1.getNotes());
    verify(slotRepo).save(slot1);
  }

  @Test
  void updateSlot_throwsWhenBookedAndNotUnbooking() {
      when(slotRepo.findById(101L)).thenReturn(Optional.of(slot2)); 

      AppointmentSlotDTO patch = new AppointmentSlotDTO();
      patch.setBooked(true);

      var ex = assertThrows(IllegalStateException.class, () -> service.updateSlot(101L, patch));
      assertTrue(ex.getMessage().contains("Cannot update a booked slot."));
      verify(slotRepo, never()).save(any());
  }


  @Test
  void updateSlot_allowsUnbookingBookedSlot() {
    when(slotRepo.findById(101L)).thenReturn(Optional.of(slot2)); 
    when(slotRepo.save(slot2)).thenReturn(slot2);
    when(slotMapper.toDto(slot2)).thenReturn(dto1);

    AppointmentSlotDTO patch = new AppointmentSlotDTO();
    trySet(patch, "booked", false); 

    var result = service.updateSlot(101L, patch);

    assertSame(dto1, result);
    assertFalse(slot2.isBooked());
    verify(slotRepo).save(slot2);
  }

  @Test
  void updateSlot_changesDoctor_whenDoctorIdProvided() {
    when(slotRepo.findById(100L)).thenReturn(Optional.of(slot1));
    when(slotRepo.save(slot1)).thenReturn(slot1);
    when(slotMapper.toDto(slot1)).thenReturn(dto1);

    Doctor newDoc = new Doctor();
    newDoc.setId(8L);
    when(doctorRepo.findById(8L)).thenReturn(Optional.of(newDoc));

    AppointmentSlotDTO patch = new AppointmentSlotDTO();
    trySet(patch, "booked", false);
    trySet(patch, "doctorId", 8L);

    var result = service.updateSlot(100L, patch);

    assertSame(dto1, result);
    assertSame(newDoc, slot1.getDoctor());
    verify(doctorRepo).findById(8L);
  }

  @Test
  void updateSlot_throwsWhenDoctorIdProvidedButDoctorMissing() {
    when(slotRepo.findById(100L)).thenReturn(Optional.of(slot1));

    AppointmentSlotDTO patch = new AppointmentSlotDTO();
    trySet(patch, "booked", false);
    trySet(patch, "doctorId", 999L);

    when(doctorRepo.findById(999L)).thenReturn(Optional.empty());

    var ex = assertThrows(EntityNotFoundException.class, () -> service.updateSlot(100L, patch));
    assertTrue(ex.getMessage().contains("Doctor not found"));
    verify(slotRepo, never()).save(any());
  }

  @Test
  void updateSlot_throwsWhenSlotMissing() {
    when(slotRepo.findById(404L)).thenReturn(Optional.empty());

    var ex = assertThrows(EntityNotFoundException.class, () -> service.updateSlot(404L, new AppointmentSlotDTO()));
    assertTrue(ex.getMessage().contains("Appointment slot not found"));
  }

  // ----------- deleteSlot -----------

  @Test
  void deleteSlot_deletesWhenExists() {
    when(slotRepo.existsById(100L)).thenReturn(true);

    service.deleteSlot(100L);

    verify(slotRepo).existsById(100L);
    verify(slotRepo).deleteById(100L);
  }

  @Test
  void deleteSlot_throwsWhenMissing() {
    when(slotRepo.existsById(999L)).thenReturn(false);

    var ex = assertThrows(EntityNotFoundException.class, () -> service.deleteSlot(999L));
    assertTrue(ex.getMessage().contains("Appointment slot not found"));
    verify(slotRepo, never()).deleteById(anyLong());
  }

  // --- helper: tolerate DTOs without setters/builders ---
  private static void trySet(Object target, String field, Object value) {
    try {
      var m = target.getClass().getMethod("set" + Character.toUpperCase(field.charAt(0)) + field.substring(1), value.getClass());
      m.invoke(target, value);
    } catch (Exception ignored) {}
  }


@Test
void createSlot_allowsAdjacent_noOverlap() {
  // Arrange
  var doctor = new Doctor(); doctor.setId(1L);
  when(doctorRepo.findById(1L)).thenReturn(java.util.Optional.of(doctor));

  var dto = new SlotCreateDTO();
  dto.setDoctorId(1L);
  dto.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 30));
  dto.setEndTime(LocalDateTime.of(2025, 1, 1, 11,  0));

  var entity = new AppointmentSlot();
  entity.setStartTime(dto.getStartTime());
  entity.setEndTime(dto.getEndTime());
  when(slotMapper.toEntity(dto)).thenReturn(entity);

  // Adjacent is OK → repository returns false (no overlap)
  when(slotRepo.existsOverlapping(doctor, dto.getStartTime(), dto.getEndTime()))
      .thenReturn(false);

  when(slotRepo.save(any(AppointmentSlot.class))).thenAnswer(inv -> inv.getArgument(0));
  var outDto = new AppointmentSlotDTO();
  when(slotMapper.toDto(any(AppointmentSlot.class))).thenReturn(outDto);

  // Act
  var res = service.createSlot(dto);

  // Assert
  assertSame(outDto, res);
  verify(slotRepo, times(1)).existsOverlapping(doctor, dto.getStartTime(), dto.getEndTime());
  verify(slotRepo, times(1)).save(any(AppointmentSlot.class));
}

@Test
void createSlot_rejectsPartialOverlap_sameDoctor() {
  // Arrange
  var doctor = new Doctor(); doctor.setId(1L);
  when(doctorRepo.findById(1L)).thenReturn(java.util.Optional.of(doctor));

  var dto = new SlotCreateDTO();
  dto.setDoctorId(1L);
  dto.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 15)); // overlaps [10:00,10:30]
  dto.setEndTime(LocalDateTime.of(2025, 1, 1, 10, 45));

  var entity = new AppointmentSlot();
  entity.setStartTime(dto.getStartTime());
  entity.setEndTime(dto.getEndTime());
  when(slotMapper.toEntity(dto)).thenReturn(entity);

  // Overlap → repository returns true
  when(slotRepo.existsOverlapping(doctor, dto.getStartTime(), dto.getEndTime()))
      .thenReturn(true);

  // Act + Assert
  var ex = assertThrows(IllegalStateException.class, () -> service.createSlot(dto));
  assertTrue(ex.getMessage().contains("Overlapping slot"));
  verify(slotRepo, never()).save(any());
}

@Test
void createSlot_rejectsExactMatch_sameDoctor() {
  var doctor = new Doctor(); doctor.setId(1L);
  when(doctorRepo.findById(1L)).thenReturn(java.util.Optional.of(doctor));

  var dto = new SlotCreateDTO();
  dto.setDoctorId(1L);
  dto.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
  dto.setEndTime(LocalDateTime.of(2025, 1, 1, 10, 30));

  var entity = new AppointmentSlot();
  entity.setStartTime(dto.getStartTime());
  entity.setEndTime(dto.getEndTime());
  when(slotMapper.toEntity(dto)).thenReturn(entity);

  when(slotRepo.existsOverlapping(doctor, dto.getStartTime(), dto.getEndTime()))
      .thenReturn(true); // exact match is overlap

  var ex = assertThrows(IllegalStateException.class, () -> service.createSlot(dto));
  assertTrue(ex.getMessage().contains("Overlapping slot"));
  verify(slotRepo, never()).save(any());
}

@Test
void createSlot_sameTimeDifferentDoctor_isOK() {

  var doctorA = new Doctor(); doctorA.setId(1L);
  when(doctorRepo.findById(1L)).thenReturn(java.util.Optional.of(doctorA));

  var dto = new SlotCreateDTO();
  dto.setDoctorId(1L);
  dto.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
  dto.setEndTime(LocalDateTime.of(2025, 1, 1, 10, 30));

  var entity = new AppointmentSlot();
  entity.setStartTime(dto.getStartTime());
  entity.setEndTime(dto.getEndTime());
  when(slotMapper.toEntity(dto)).thenReturn(entity);

  when(slotRepo.existsOverlapping(doctorA, dto.getStartTime(), dto.getEndTime()))
      .thenReturn(false);

  when(slotRepo.save(any(AppointmentSlot.class))).thenAnswer(inv -> inv.getArgument(0));
  var outDto = new AppointmentSlotDTO();
  when(slotMapper.toDto(any(AppointmentSlot.class))).thenReturn(outDto);

  var res = service.createSlot(dto);

  assertSame(outDto, res);
  verify(slotRepo).existsOverlapping(doctorA, dto.getStartTime(), dto.getEndTime());
  verify(slotRepo).save(any(AppointmentSlot.class));
}

}
