package com.medical.bookingapi.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE) // ensure this runs before Spring Security
  public CorsFilter corsFilter() {
    CorsConfiguration cfg = new CorsConfiguration();

    // Allow your production frontend and local dev
    cfg.setAllowedOrigins(List.of(
        "https://medicalbooking.koyeb.app",
        "http://localhost:5173"
    ));

    // If you use cookie/session auth, set to true; for Bearer tokens keep false
    cfg.setAllowCredentials(false);

    cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
    cfg.setAllowedHeaders(List.of("*"));
    cfg.setExposedHeaders(List.of("Authorization","Content-Type"));
    cfg.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return new CorsFilter(source);
  }
}
