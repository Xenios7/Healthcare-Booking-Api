package com.medical.bookingapi.service;

import com.medical.bookingapi.model.User;
import com.medical.bookingapi.repository.UserRepository;
import com.medical.bookingapi.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new CustomUserDetails(user); 
    }
}

// A user sends a login request (/login or your custom auth endpoint)

// Spring Security calls loadUserByUsername(email)

// Your service fetches the user and returns their details (via CustomUserDetails)

// Spring checks the hashed password

// If valid → the user is authenticated and added to the SecurityContext

