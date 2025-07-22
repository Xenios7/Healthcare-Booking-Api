package com.medical.bookingapi.security;

import com.medical.bookingapi.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // Convert user's role to GrantedAuthority
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword_hash(); // match your entity field
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // login will use email
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // change if you add account expiration logic
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // change if you add locking
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // change if you add credential expiration logic
    }

    @Override
    public boolean isEnabled() {
        return true; // add a field like 'isEnabled' if needed
    }

    public User getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomUserDetails that)) return false;
        return Objects.equals(user.getId(), that.user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(user.getId());
    }
}
