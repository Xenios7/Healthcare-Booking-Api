package com.medical.bookingapi.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.medical.bookingapi.dto.AppointmentCreateDTO;
import com.medical.bookingapi.dto.AppointmentDTO;
import com.medical.bookingapi.service.AppointmentService;
import com.medical.bookingapi.service.AppointmentSlotService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/appointmentSlots")
@RequiredArgsConstructor
public class AppointmentSlotController {
 
    private final AppointmentSlotService appointmentSlotService;
    
    public ResponseEntity<>

}
