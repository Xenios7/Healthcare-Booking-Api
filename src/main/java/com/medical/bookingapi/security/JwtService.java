package com.medical.bookingapi.security;

import com.medical.bookingapi.model.User;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${security.jwt.secret}")
    private String secret;

    private static final Duration EXPIRATION = Duration.ofHours(10);
    private SecretKey key;

    @PostConstruct
    public void init() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("security.jwt.secret is missing or blank");
        }

        byte[] raw;
        try {
            raw = Decoders.BASE64.decode(secret);
            log.info("JWT secret decoded as Base64 ({} bytes).", raw.length);
        } catch (DecodingException e1) {
            try {
                raw = Decoders.BASE64URL.decode(secret);
                log.info("JWT secret decoded as Base64URL ({} bytes).", raw.length);
            } catch (DecodingException e2) {
                raw = secret.getBytes(StandardCharsets.UTF_8);
                log.info("JWT secret treated as raw UTF-8 ({} bytes).", raw.length);
            }
        }

        // Ensure >= 32 bytes: if shorter, derive a 32-byte material using SHA-256.
        byte[] material = raw;
        if (material.length < 32) {
            try {
                material = MessageDigest.getInstance("SHA-256").digest(material);
                log.warn("JWT secret was <32 bytes; derived 32-byte HMAC key via SHA-256.");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-256 not available", e);
            }
        }

        this.key = Keys.hmacShaKeyFor(material);
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

    public String extractUsername(String token) { return extractClaim(token, Claims::getSubject); }
    public String extractEmail(String token)    { return extractUsername(token); }
    public String extractRole(String token)     { return extractClaim(token, c -> c.get("role", String.class)); }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            Date exp = extractClaim(token, Claims::getExpiration);
            return userDetails.getUsername().equals(username) && exp.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = parser().parseClaimsJws(token).getBody();
        return resolver.apply(claims);
    }
}
