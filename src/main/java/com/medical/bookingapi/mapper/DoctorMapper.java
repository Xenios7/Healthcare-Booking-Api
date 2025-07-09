package com.medical.bookingapi.mapper;

import com.medical.bookingapi.dto.DoctorDTO;
import com.medical.bookingapi.dto.DoctorRegistrationDTO;
import com.medical.bookingapi.model.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    DoctorDTO toDto(Doctor doctor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true) // Let the DB handle it
    @Mapping(target = "lastLogin", ignore = true) // Let the DB handle it
    @Mapping(target = "password_hash", ignore = true) // You will manually hash and set this in the service
    Doctor toEntity(DoctorRegistrationDTO dto);
    
}
