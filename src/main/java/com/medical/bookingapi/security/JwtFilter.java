package com.medical.bookingapi.security;

import com.medical.bookingapi.service.CustomUserDetailsService;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Extract the Authorization header
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwt;
        final String username;

        // 2. If no Authorization header or it doesn't start with "Bearer ", skip the filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract the token from the header
        jwt = authHeader.substring(7); // Removes "Bearer " prefix

        // 4. Extract username (email) from the token using the JwtService
        username = jwtService.extractUsername(jwt);

        // 5. If we have a username and the user is not yet authenticated in this context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 6. Load the user from the DB
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 7. Validate the token against the user’s details
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // 8. Create an authentication token
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // 9. Set additional authentication details (e.g., IP address, session info)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 10. Set the authentication in the security context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 11. Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}
