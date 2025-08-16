package com.medical.bookingapi.security;

import com.medical.bookingapi.model.User;

// RIGHT
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    // Put a Base64-encoded 32+ byte value in application*.yml: security.jwt.secret: "...."
    @Value("${security.jwt.secret}")
    private String secretBase64;

    private static final Duration EXPIRATION = Duration.ofHours(10);

    private SecretKey key;

    @PostConstruct
    void init() {
        String s = secretBase64 == null ? "" : secretBase64.trim();
        byte[] keyBytes;
        String decodedAs;

        try {
            // Standard Base64 (uses + and /)
            keyBytes = Decoders.BASE64.decode(s);
            decodedAs = "Base64";
        } catch (DecodingException e1) {
            try {
                // URL-safe Base64 (uses - and _)
                keyBytes = Decoders.BASE64URL.decode(s);
                decodedAs = "Base64URL";
            } catch (DecodingException e2) {
                // Fallback: treat as plain text bytes
                keyBytes = s.getBytes(StandardCharsets.UTF_8);
                decodedAs = "plain UTF-8";
            }
        }

        log.info("JWT key bytes: {} (decoded as {})", keyBytes.length, decodedAs);

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                "security.jwt.secret must be a 32+ byte key (or Base64/Base64URL of that). "
                + "Current length: " + keyBytes.length + " bytes"
            );
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
    }



    private JwtParser parser() {
        return Jwts.parserBuilder().setSigningKey(key).build();
    }

    public String generateToken(UserDetails userDetails) {
        User user = ((CustomUserDetails) userDetails).getUser();
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole())   
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(EXPIRATION)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractEmail(String token) {
        return extractUsername(token);
    }

    public String extractRole(String token) {
        return extractClaim(token, c -> c.get("role", String.class));
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            Date exp = extractClaim(token, Claims::getExpiration);
            return userDetails.getUsername().equals(username) && exp.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false; // malformed/expired/invalid signature -> not valid
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = parser().parseClaimsJws(token).getBody();
        return resolver.apply(claims);
    }
}
