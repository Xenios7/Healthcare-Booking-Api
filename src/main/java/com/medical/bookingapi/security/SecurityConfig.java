package com.medical.bookingapi.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  private final JwtFilter jwtFilter;
  private final UserDetailsService userDetailsService;

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  DaoAuthenticationProvider authenticationProvider(PasswordEncoder enc) {
    var p = new DaoAuthenticationProvider();
    p.setUserDetailsService(userDetailsService);
    p.setPasswordEncoder(enc);
    return p;
  }

  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider provider) throws Exception {
    return http
        .authenticationProvider(provider)
        .cors(Customizer.withDefaults())                 // enable CORS
        .csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // preflight
            .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
            .requestMatchers("/api/users/login", "/api/auth/**", "/auth/**").permitAll()
            .requestMatchers("/h2-console/**").permitAll()
            .requestMatchers("/api/admins/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/doctors/me").hasAnyRole("DOCTOR","ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/doctors/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/doctors").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT,  "/api/doctors/**").hasAnyRole("DOCTOR","ADMIN")
            .requestMatchers("/api/patients/**").authenticated()
            .requestMatchers(HttpMethod.POST, "/api/appointments").hasRole("PATIENT")
            .requestMatchers(HttpMethod.PUT, "/api/appointments/**").hasAnyRole("DOCTOR","ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/appointments/**").authenticated()
            .requestMatchers(HttpMethod.POST, "/api/appointmentSlots").hasAnyRole("DOCTOR","ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/appointmentSlots/**").hasAnyRole("DOCTOR","ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/appointmentSlots/**").hasAnyRole("DOCTOR","ADMIN")
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .headers(h -> h.frameOptions(f -> f.sameOrigin()))
        .build();
  }

  // *** Add this: global CORS rules ***
  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();
    cfg.setAllowedOrigins(List.of(
        "https://right-renelle-xenios-886dcd55.koyeb.app", // your frontend
        "http://localhost:5173"                             // local dev (Vite)
    ));
    cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
    cfg.setAllowedHeaders(List.of("Authorization","Content-Type","Accept","Origin","X-Requested-With"));
    cfg.setExposedHeaders(List.of("Authorization","Content-Type"));
    cfg.setAllowCredentials(true);
    cfg.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
  }
}
