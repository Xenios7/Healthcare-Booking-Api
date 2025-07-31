package com.medical.bookingapi.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.medical.bookingapi.dto.PatientDTO;
import com.medical.bookingapi.dto.PatientRegistrationDTO;
import com.medical.bookingapi.service.PatientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {
 
    private final PatientService patientService;

    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {

        PatientDTO patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patient);
    }

    @GetMapping("/email")
    public ResponseEntity<PatientDTO> getPatientByEmail(@RequestParam String email) { //Maps HTTP GET requests to /api/patients/email
        
        return patientService.findByEmail(email)
                .map(ResponseEntity::ok) // If the Optional has a value, wrap it in ResponseEntity.ok(...) (200 OK with body)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));
    }

    @GetMapping("/blood-type/{bloodType}")
    public ResponseEntity<List<PatientDTO>> getPatientsByBloodType(@PathVariable String bloodType) {

        List<PatientDTO> patients = patientService.findByBloodType(bloodType);
        return ResponseEntity.ok(patients);
    }

    @PostMapping
    public ResponseEntity<PatientDTO> createPatient(@RequestBody @Valid PatientRegistrationDTO dto){

        PatientDTO patient = patientService.createPatient(dto);
        return ResponseEntity.ok(patient);        

    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDTO> updatePatient(@PathVariable Long id, @RequestBody @Valid PatientDTO dto){

        PatientDTO patient = patientService.updatePatient(id, dto);
        return ResponseEntity.ok(patient);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id){

        patientService.deletePatient(id);
        return ResponseEntity.noContent().build(); // 204 No Content

    }




}   
