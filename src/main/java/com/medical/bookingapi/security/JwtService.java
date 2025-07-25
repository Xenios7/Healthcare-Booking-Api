package com.medical.bookingapi.security;

import com.medical.bookingapi.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 hours
    private static final String SECRET = "supersecretkeysupersecretkeysupersecretkey"; 

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(UserDetails userDetails) {
        CustomUserDetails customUser = (CustomUserDetails) userDetails;
        User user = customUser.getUser(); 

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole()) // Add role as JWT claim
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String extractedEmail = extractEmail(token);
        return (extractedEmail.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractEmail(token); // Reuse logic
    }
}
