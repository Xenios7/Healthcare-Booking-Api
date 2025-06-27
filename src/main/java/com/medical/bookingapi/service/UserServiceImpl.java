package com.medical.bookingapi.service;

import com.medical.bookingapi.dto.UserResponseDTO;
import com.medical.bookingapi.mapper.UserMapper;
import com.medical.bookingapi.model.User;
import com.medical.bookingapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return userMapper.toDto(user);
    }


    @Override
    public void updateLastLogin(User user) {
        User existingUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        existingUser.setLastLogin(LocalDateTime.now());
        userRepository.save(existingUser);
    }


    
}
