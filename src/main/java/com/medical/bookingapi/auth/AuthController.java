package com.medical.bookingapi.auth;

import com.medical.bookingapi.dto.RegisterRequestDTO;
import com.medical.bookingapi.dto.UserLoginDTO;
import com.medical.bookingapi.dto.UserResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequestDTO request){
        
        authService.registerPatient(request);
        return ResponseEntity.status(201).body("Patient registered successfully.");

    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@RequestBody @Valid UserLoginDTO login){

        UserResponseDTO response = authService.login(login);
        return ResponseEntity.ok(response);

    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleError(Exception ex) {
        return ResponseEntity.status(500).body("Something went wrong: " + ex.getMessage());
    }


}
