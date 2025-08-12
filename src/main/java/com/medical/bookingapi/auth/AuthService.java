package com.medical.bookingapi.auth;

import com.medical.bookingapi.dto.*;
import com.medical.bookingapi.model.*;
import com.medical.bookingapi.repository.UserRepository;
import com.medical.bookingapi.security.CustomUserDetails;
import com.medical.bookingapi.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final JwtService jwtService;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  // Patient self-signup
  public void registerPatient(RegisterRequestDTO req) {
    if (userRepository.existsByEmail(req.getEmail())) {
      throw new IllegalArgumentException("Email already in use");
    }
    Patient p = new Patient();
    p.setFirstName(req.getFirstName());
    p.setLastName(req.getLastName());
    p.setEmail(req.getEmail());
    p.setPassword_hash(passwordEncoder.encode(req.getPassword()));
    p.setRole("PATIENT");
    userRepository.save(p);
  }

  // Admin creates Patient with extra fields
  public void registerPatientByAdmin(PatientRegistrationDTO dto) {
    if (userRepository.existsByEmail(dto.getEmail())) {
      throw new IllegalArgumentException("Email already in use");
    }
    Patient p = new Patient();
    p.setFirstName(dto.getFirstName());
    p.setLastName(dto.getLastName());
    p.setEmail(dto.getEmail());
    p.setPassword_hash(passwordEncoder.encode(dto.getPassword()));
    p.setRole("PATIENT");
    p.setDateOfBirth(dto.getDateOfBirth());
    p.setBloodType(dto.getBloodType());
    p.setAllergies(dto.getAllergies());
    p.setInsuranceId(dto.getInsuranceId());
    userRepository.save(p);
  }

  // Admin creates Doctor
  public void registerDoctor(DoctorRegistrationDTO dto) {
    if (userRepository.existsByEmail(dto.getEmail())) {
      throw new IllegalArgumentException("Email already in use");
    }
    Doctor d = new Doctor();
    d.setFirstName(dto.getFirstName());
    d.setLastName(dto.getLastName());
    d.setEmail(dto.getEmail());
    d.setPassword_hash(passwordEncoder.encode(dto.getPassword()));
    d.setRole("DOCTOR");
    d.setLocation(dto.getLocation());
    d.setSpeciality(dto.getSpeciality());
    d.setLicenseNumber(dto.getLicenseNumber());
    userRepository.save(d);
  }

  // Admin creates Admin
  public void registerAdmin(RegisterRequestDTO dto) {
    if (userRepository.existsByEmail(dto.getEmail())) {
      throw new IllegalArgumentException("Email already in use");
    }
    Admin a = new Admin();              // class com.medical.bookingapi.model.Admin extends User
    a.setFirstName(dto.getFirstName());
    a.setLastName(dto.getLastName());
    a.setEmail(dto.getEmail());
    a.setPassword_hash(passwordEncoder.encode(dto.getPassword()));
    a.setRole("ADMIN");
    userRepository.save(a);
  }

  public UserResponseDTO login(UserLoginDTO request) {
    try {
      var auth = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
      );
      var userDetails = (CustomUserDetails) auth.getPrincipal();
      var user = userDetails.getUser();
      var token = jwtService.generateToken(userDetails);
      return new UserResponseDTO(
          user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole(), token
      );
    } catch (AuthenticationException e) {
      throw new RuntimeException("Invalid credentials");
    }
  }
}
