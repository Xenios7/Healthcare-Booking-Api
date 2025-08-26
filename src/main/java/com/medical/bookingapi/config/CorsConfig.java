package com.medical.bookingapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE) // run before Spring Security
  public CorsFilter corsFilter() {
    CorsConfiguration cfg = new CorsConfiguration();

    // If your frontend subdomain changes, this keeps working:
    cfg.setAllowedOriginPatterns(List.of(
        "https://*.koyeb.app",
        "http://localhost:5173"
    ));
    // Or lock it to the exact URL if you prefer:
    // cfg.setAllowedOrigins(List.of("https://right-renelle-xenios-886dcd55.koyeb.app", "http://localhost:5173"));

    cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
    cfg.setAllowedHeaders(List.of("*"));
    cfg.setExposedHeaders(List.of("Authorization","Content-Type"));
    cfg.setAllowCredentials(true);
    cfg.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return new CorsFilter(source);
  }
}
