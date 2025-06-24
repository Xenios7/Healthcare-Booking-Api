package com.medical.bookingapi.mapper;

import com.medical.bookingapi.dto.PatientDTO;
import com.medical.bookingapi.dto.PatientRegistrationDTO;
import com.medical.bookingapi.model.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    PatientDTO toDto(Patient patient);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true) //Don’t map these fields. Let the database handle them.
    @Mapping(target = "lastLogin", ignore = true) //Don’t map these fields. Let the database handle them.
    @Mapping(target = "password_hash", ignore = true) // don't want to map it directly from the DTO, the DTO has a plain password.

    Patient toEntity(PatientRegistrationDTO dto);

}