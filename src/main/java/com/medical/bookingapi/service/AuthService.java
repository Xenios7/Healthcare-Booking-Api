package com.medical.bookingapi.service;

import com.medical.bookingapi.dto.RegisterRequestDTO;
import com.medical.bookingapi.dto.UserLoginDTO;
import com.medical.bookingapi.dto.UserResponseDTO;
import com.medical.bookingapi.model.Doctor;
import com.medical.bookingapi.model.Patient;
import com.medical.bookingapi.model.User;
import com.medical.bookingapi.repository.UserRepository;
import com.medical.bookingapi.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public void register(RegisterRequestDTO request) {
        User user = buildUserFromRequest(request);
        userRepository.save(user);
    }

    private User buildUserFromRequest(RegisterRequestDTO request) {
        return switch (request.getRole().toUpperCase()) {
            case "PATIENT" -> {
                Patient patient = new Patient();
                patient.setFirstName(request.getFirstName());
                patient.setLastName(request.getLastName());
                patient.setEmail(request.getEmail());
                patient.setPassword_hash(passwordEncoder.encode(request.getPassword()));
                patient.setRole("PATIENT");
                yield patient;
            }
            case "DOCTOR" -> {
                Doctor doctor = new Doctor();
                doctor.setFirstName(request.getFirstName());
                doctor.setLastName(request.getLastName());
                doctor.setEmail(request.getEmail());
                doctor.setPassword_hash(passwordEncoder.encode(request.getPassword()));
                doctor.setRole("DOCTOR");
                yield doctor;
            }
            default -> throw new IllegalArgumentException("Invalid role: " + request.getRole());
        };
    }

    public UserResponseDTO login(UserLoginDTO request) {
        try {
            org.springframework.security.core.Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            return new UserResponseDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(), null
            );

        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid credentials"); 
        }
    
    }
}
