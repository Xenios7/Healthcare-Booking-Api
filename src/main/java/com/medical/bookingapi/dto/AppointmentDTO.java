package com.medical.bookingapi.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AppointmentDTO {
    private Long id;

    private Long patientId;
    private String patientName;

    private Long doctorId;
    private String doctorName;
    private String doctorSpeciality;

    private Long slotId;
    private LocalDateTime slotStartTime;
    private LocalDateTime slotEndTime;

    private String status;
    private String notes;
    private LocalDateTime createdAt;
}
