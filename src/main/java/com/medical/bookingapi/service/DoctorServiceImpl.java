package com.medical.bookingapi.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.medical.bookingapi.dto.DoctorDTO;
import com.medical.bookingapi.dto.DoctorProfileUpdateDTO;
import com.medical.bookingapi.dto.DoctorRegistrationDTO;
import com.medical.bookingapi.mapper.DoctorMapper;
import com.medical.bookingapi.model.Doctor;
import com.medical.bookingapi.repository.DoctorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;
    private final PasswordEncoder passwordEncoder; 
        
    @Override
    public DoctorDTO findById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return doctorMapper.toDto(doctor);
    }


    @Override
    public DoctorDTO findByEmail(String email) {
  
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return doctorMapper.toDto(doctor);

    }

    @Override
    public List<DoctorDTO> findBySpeciality(String specialty) {

        List<Doctor> doctors = doctorRepository.findBySpeciality(specialty);
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
        // Hash the password
        doctor.setPassword_hash(passwordEncoder.encode(dto.getPassword()));
        // Set the role
        doctor.setRole("DOCTOR");
        return doctorMapper.toDto(doctorRepository.save(doctor));
    }

    public DoctorDTO me() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Doctor me = doctorRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Doctor not found"));
        return doctorMapper.toDto(me);
    }

    public DoctorDTO updateMyProfile(DoctorProfileUpdateDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Doctor me = doctorRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Doctor not found"));

        me.setFirstName(dto.getFirstName());
        me.setLastName(dto.getLastName());
        me.setSpeciality(dto.getSpeciality());
        me.setLocation(dto.getLocation());
        me.setLicenseNumber(dto.getLicenseNumber());

        return doctorMapper.toDto(doctorRepository.save(me));
    }

    
}
