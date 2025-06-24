package com.medical.bookingapi.dto;

import lombok.Data;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

@Data
public class SlotCreateDTO {
 
    @NotNull
    private Long doctorId;

    @NotNull
    @Future
    private LocalDateTime startTime;

    @NotNull
    @Future    
    private LocalDateTime endTime;
    private String notes;

}
