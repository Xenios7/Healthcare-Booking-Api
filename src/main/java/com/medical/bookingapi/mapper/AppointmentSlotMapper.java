package com.medical.bookingapi.mapper;

import com.medical.bookingapi.dto.AppointmentSlotDTO;
import com.medical.bookingapi.dto.SlotCreateDTO;
import com.medical.bookingapi.model.AppointmentSlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentSlotMapper {

    // Convert entity to DTO for API response
    @Mapping(source = "doctor.id", target = "doctorId")
    AppointmentSlotDTO toDto(AppointmentSlot slot);

    // Convert create DTO to entity (doctor will be set manually in service)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    AppointmentSlot toEntity(SlotCreateDTO dto);
}
