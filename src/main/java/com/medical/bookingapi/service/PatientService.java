package com.medical.bookingapi.service;

import java.util.List;
import java.util.Optional;


import com.medical.bookingapi.dto.PatientDTO;
import com.medical.bookingapi.dto.PatientRegistrationDTO;

public interface PatientService {

    Optional<PatientDTO> findByEmail(String email);
    List<PatientDTO> findByBloodType(String bloodType);

    PatientDTO createPatient(PatientRegistrationDTO dto);
    PatientDTO updatePatient(Long id, PatientDTO dto);
    PatientDTO getPatientById(Long id);

    void deletePatient(Long id);
    
}
