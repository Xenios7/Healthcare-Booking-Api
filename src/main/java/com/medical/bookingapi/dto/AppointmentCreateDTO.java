package com.medical.bookingapi.dto;
import lombok.Data;

//AppointmentCreateDTO is the form a patient fills out at the front desk, only what’s necessary to book the appointment, and nothing more

@Data
public class AppointmentCreateDTO {
    
    private Long patientId;
    private Long doctorId;
    private Long slotId;
    private String notes;
}
