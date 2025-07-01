package com.medical.bookingapi.controller;

import com.medical.bookingapi.dto.DoctorDTO;
import com.medical.bookingapi.dto.DoctorRegistrationDTO;
import com.medical.bookingapi.service.DoctorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {
    
    private final DoctorService doctorService;

    @GetMapping("/{id}")
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable Long id){

        DoctorDTO doctor = doctorService.findById(id);
        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/email")
    public ResponseEntity<DoctorDTO> getDoctorByEmail(@RequestParam("email") String email){

        DoctorDTO doctor = doctorService.findByEmail(email);
        return ResponseEntity.ok(doctor);

    }

    @GetMapping("/speciality/{speciality}")
    public ResponseEntity<List<DoctorDTO>> getDoctorsBySpeciality(@PathVariable String speciality){

        List<DoctorDTO> doctors = doctorService.findBySpeciality(speciality);
        return ResponseEntity.ok(doctors);

    }

    @PostMapping
    public ResponseEntity<DoctorDTO> createDoctor(@RequestBody @Valid DoctorRegistrationDTO dto){

        DoctorDTO doctor = doctorService.createDoctor(dto);
        return ResponseEntity.ok(doctor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorDTO> updateDoctor(@PathVariable Long id, @RequestBody @Valid DoctorDTO dto){

        DoctorDTO doctor = doctorService.updateDoctor(id, dto);
        return ResponseEntity.ok(doctor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id){

        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();

    }


}
