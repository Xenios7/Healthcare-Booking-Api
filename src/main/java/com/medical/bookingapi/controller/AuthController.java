package com.medical.bookingapi.controller;

import com.medical.bookingapi.dto.RegisterRequestDTO;
import com.medical.bookingapi.dto.UserLoginDTO;
import com.medical.bookingapi.dto.UserResponseDTO;
import com.medical.bookingapi.service.AuthService;

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
        
        authService.register(request);
        return ResponseEntity.ok("User registered successfully.");

    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@RequestBody UserLoginDTO login){

        UserResponseDTO response = authService.login(login);
        return ResponseEntity.ok(response);

    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleError(Exception ex) {
        return ResponseEntity.status(500).body("Something went wrong: " + ex.getMessage());
    }


}
