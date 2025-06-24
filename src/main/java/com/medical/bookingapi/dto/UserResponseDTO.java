package com.medical.bookingapi.dto;
import lombok.Data;

@Data
public class UserResponseDTO {

    private Long id;     
    private String name;
    private String role;
    private String token; //For JWT

}
