package com.medical.bookingapi.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.medical.bookingapi.dto.DoctorDTO;
import com.medical.bookingapi.dto.DoctorRegistrationDTO;
import com.medical.bookingapi.mapper.DoctorMapper;
import com.medical.bookingapi.model.Doctor;
import com.medical.bookingapi.repository.DoctorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;

    @Override
    public DoctorDTO findByEmail(String email) {
  
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return doctorMapper.toDto(doctor);

    }

    @Override
    public List<DoctorDTO> findBySpecialty(String specialty) {

        List<Doctor> doctors = doctorRepository.findBySpecialty(specialty);
        if(doctors.isEmpty()){
            throw new UsernameNotFoundException("No doctors found");
        }
        return doctors.stream()
                .map(doctorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public DoctorDTO createDoctor(DoctorRegistrationDTO dto) {

        Doctor doctor = doctorMapper.toEntity(dto);
        return doctorMapper.toDto(doctorRepository.save(doctor));
    }

    @Override
    public DoctorDTO updateDoctor(Long id, DoctorDTO dto) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found"));

        // We update the fields manually
        doctor.setFirstName(dto.getFirstName());
        doctor.setLastName(dto.getLastName());
        doctor.setSpeciality(dto.getSpeciality());
        doctor.setEmail(dto.getEmail());

        return doctorMapper.toDto(doctorRepository.save(doctor));
    }

    @Override
    public void deleteDoctor(Long id) {

        if(!doctorRepository.existsById(id)){
            throw new UsernameNotFoundException("Doctor not found");
        }

        doctorRepository.deleteById(id);
    }
    


}
