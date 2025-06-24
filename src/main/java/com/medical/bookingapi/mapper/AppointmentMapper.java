package com.medical.bookingapi.mapper;

import com.medical.bookingapi.dto.AppointmentDTO;
import com.medical.bookingapi.dto.AppointmentCreateDTO;
import com.medical.bookingapi.model.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {
    
    @Mapping(source = "doctor.id", target = "doctorId")
    @Mapping(source = "doctor.firstName", target = "doctorName")
    @Mapping(source = "doctor.speciality", target = "doctorSpeciality")
    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(source = "patient.firstName", target = "patientName")
    @Mapping(source = "slot.id", target = "slotId")
    @Mapping(source = "slot.startTime", target = "slotStartTime")
    @Mapping(source = "slot.endTime", target = "slotEndTime")
    AppointmentDTO toDto(Appointment appointment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "slot", ignore = true)
    Appointment toEntity(AppointmentCreateDTO dto);
}
