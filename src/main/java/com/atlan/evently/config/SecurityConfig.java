package com.atlan.evently.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    // Admin endpoints require ADMIN role
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                    
                    // Public endpoints - no authentication required
                    .requestMatchers(
                        "/api/v1/events/**",           // Event browsing
                        "/api/v1/users/register",     // User registration  
                        "/api/v1/users/login",        // User login (if implemented)
                        "/api/v1/users/check-email/**", // Email availability check
                        "/actuator/**",               // Health checks
                        "/swagger-ui/**",             // API documentation
                        "/v3/api-docs/**"            // OpenAPI spec
                    ).permitAll()
                    
                    // User-specific endpoints - could require authentication in production
                    // For MVP, we'll make them public for testing
                    .requestMatchers(
                        "/api/v1/users/**",           // User profile access
                        "/api/v1/bookings/**"        // Booking operations
                    ).permitAll()
                    
                    // All other requests require authentication
                    .anyRequest().authenticated()
            )
            .addFilterBefore(new CustomTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static class CustomTokenFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                        @NonNull HttpServletResponse response,
                                        @NonNull FilterChain filterChain) throws IOException, ServletException {
            String adminToken = request.getHeader("X-Admin-Token");
            if (adminToken != null && "admin-secret".equals(adminToken)) {
                UserDetails userDetails = User.withUsername("admin")
                        .password("unused") // Password not used with token
                        .roles("ADMIN")
                        .build();
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                    );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        }
    }
}