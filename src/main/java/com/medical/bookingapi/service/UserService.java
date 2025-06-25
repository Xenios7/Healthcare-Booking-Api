package com.medical.bookingapi.service;

import com.medical.bookingapi.dto.UserResponseDTO;
import com.medical.bookingapi.model.User;

public interface UserService {
    
    UserResponseDTO getUserByEmail(String email);
    void updateLastLogin(User user);
}
