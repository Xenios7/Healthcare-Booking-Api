package com.medical.bookingapi.service;

import java.util.List;

import com.medical.bookingapi.dto.DoctorDTO;
import com.medical.bookingapi.dto.DoctorRegistrationDTO;

public interface DoctorService {

    DoctorDTO findByEmail(String email);
    List<DoctorDTO> findBySpecialty(String specialty);
    DoctorDTO createDoctor(DoctorRegistrationDTO dto);
    DoctorDTO updateDoctor(Long id, DoctorDTO dto);
    void deleteDoctor(Long id);
    
}
