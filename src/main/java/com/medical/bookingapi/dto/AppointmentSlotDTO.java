package com.medical.bookingapi.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentSlotDTO {
    
    private Long id;
    private Long doctorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isBooked;
    private String notes;

}
