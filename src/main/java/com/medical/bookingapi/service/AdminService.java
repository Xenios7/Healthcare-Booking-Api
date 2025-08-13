package com.medical.bookingapi.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import com.medical.bookingapi.dto.PatientAdminUpdateDTO;
import com.medical.bookingapi.dto.PatientDTO;
import com.medical.bookingapi.dto.DoctorAdminUpdateDTO;
import com.medical.bookingapi.dto.DoctorDTO;
import com.medical.bookingapi.model.Patient;
import com.medical.bookingapi.model.Doctor;
import com.medical.bookingapi.repository.PatientRepository;
import com.medical.bookingapi.repository.DoctorRepository;
import com.medical.bookingapi.mapper.PatientMapper;
import com.medical.bookingapi.mapper.DoctorMapper;

@Service
@Validated
@PreAuthorize("hasRole('ADMIN')") // requires ROLE_ADMIN authority at runtime
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

  private final PatientRepository patientRepo;
  private final DoctorRepository doctorRepo;
  private final PatientMapper patientMapper;
  private final DoctorMapper doctorMapper;

  // ---------- PATIENTS ----------

    @Transactional
    public PatientDTO updatePatient(@NotNull Long id, @Valid @NotNull PatientAdminUpdateDTO dto) {
        Patient p = patientRepo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Patient not found"));

        // update only non-null fields from the admin DTO
        if (dto.getDateOfBirth() != null) p.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getBloodType()   != null) p.setBloodType(dto.getBloodType());
        if (dto.getAllergies()   != null) p.setAllergies(dto.getAllergies());
        if (dto.getInsuranceId() != null) p.setInsuranceId(dto.getInsuranceId());

        Patient saved = patientRepo.save(p);
        return patientMapper.toDto(saved);
    }

    @Transactional
    public void deletePatient(@NotNull Long id) {
    Patient p = patientRepo.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Patient not found"));
    patientRepo.delete(p); // avoids exists() + deleteById() race
    }

  // ---------- DOCTORS ----------

    @Transactional
    public DoctorDTO updateDoctor(@NotNull Long id, @Valid @NotNull DoctorAdminUpdateDTO dto) {
        Doctor d = doctorRepo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Doctor not found"));

        // We update non-null fields from the DTO
        if (dto.getSpeciality() != null)     d.setSpeciality(dto.getSpeciality());   
        if (dto.getLocation() != null)      d.setLocation(dto.getLocation());
        if (dto.getLicenseNumber() != null) d.setLicenseNumber(dto.getLicenseNumber());

        Doctor saved = doctorRepo.save(d);
        return doctorMapper.toDto(saved);
    }

    @Transactional
    public void deleteDoctor(@NotNull Long id) {
        Doctor d = doctorRepo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Doctor not found"));
        doctorRepo.delete(d);
    }
}
