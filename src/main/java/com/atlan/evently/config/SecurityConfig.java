package com.atlan.evently.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpMethod;

/**
 * Security configuration for the Evently platform.
 * 
 * <p>Implements a multi-layered security approach:
 * <ul>
 *   <li>Admin endpoints require X-Admin-Token header authentication</li>
 *   <li>Public endpoints for event browsing and user registration</li>
 *   <li>CORS configuration for frontend integration</li>
 *   <li>Environment-based token configuration</li>
 * </ul>
 * 
 * <p><strong>Production Notes:</strong>
 * <ul>
 *   <li>Replace token-based auth with JWT for user endpoints</li>
 *   <li>Implement proper user role management</li>
 *   <li>Add rate limiting and request validation</li>
 *   <li>Enable HTTPS enforcement</li>
 * </ul>
 * 
 * @author Evently Security Team
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${evently.security.admin-token}")
    private String adminToken;

    @Value("${evently.security.cors-allowed-origins}")
    private String corsAllowedOrigins;

    /**
     * Configures password encoder for user password hashing.
     * 
     * @return BCryptPasswordEncoder with strength 12 for production security
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strength 12 for better security
    }

    /**
     * Configures the main security filter chain for HTTP requests.
     * 
     * <p>Security Rules:
     * <ul>
     *   <li>/api/v1/admin/** - Requires ADMIN role via X-Admin-Token</li>
     *   <li>/api/v1/events/** - Public access for event browsing</li>
     *   <li>/api/v1/users/register - Public for user registration</li>
     *   <li>/actuator/** - Public for health checks and monitoring</li>
     *   <li>/swagger-ui/** - Public for API documentation</li>
     *   <li>All other endpoints - Public for MVP (change in production)</li>
     * </ul>
     * 
     * @param http HttpSecurity configuration object
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    // Admin endpoints require ADMIN role
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                    
                    // Category management endpoints (admin only)
                    .requestMatchers(HttpMethod.POST, "/api/v1/categories").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasRole("ADMIN")
                    
                    // Public endpoints - no authentication required
                    .requestMatchers(
                        "/api/v1/events/**",           // Event browsing and enhanced endpoints
                        "/api/v1/categories",          // Read-only category browsing
                        "/api/v1/categories/all",      // Category listing
                        "/api/v1/categories/search",   // Category search
                        "/api/v1/categories/*",        // Get category by ID
                        "/api/v1/users/register",      // User registration  
                        "/api/v1/users/login",         // User login
                        "/api/v1/users/check-email/**", // Email availability check
                        "/actuator/**",                // Health checks and monitoring
                        "/swagger-ui/**",              // Swagger UI
                        "/swagger-ui.html",            // Swagger UI redirect
                        "/v3/api-docs/**",             // OpenAPI specification
                        "/ws/**"                       // WebSocket endpoints
                    ).permitAll()
                    
                    // User-specific endpoints - public for MVP testing
                    // TODO: Add JWT authentication for production
                    .requestMatchers(
                        "/api/v1/users/**",            // User profile access
                        "/api/v1/bookings/**",        // Booking operations
                        "/api/v1/notifications/**"    // Notification access
                    ).permitAll()
                    
                    // All other requests require authentication
                    .anyRequest().authenticated()
            )
            .addFilterBefore(new AdminTokenAuthenticationFilter(adminToken), 
                           UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) for frontend integration.
     * 
     * @return CORS configuration source with environment-based allowed origins
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Parse allowed origins from environment variable
        List<String> allowedOrigins = Arrays.asList(corsAllowedOrigins.split(","));
        configuration.setAllowedOrigins(allowedOrigins);
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("X-Total-Count", "X-Page-Count"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight response for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Custom authentication filter for admin token validation.
     * 
     * <p>Validates X-Admin-Token header against configured admin token
     * and grants ADMIN role if valid.
     * 
     * <p><strong>Security Note:</strong> This is a simple implementation for MVP.
     * Production should use proper JWT tokens with expiration and refresh.
     */
    private static class AdminTokenAuthenticationFilter extends OncePerRequestFilter {
        
        private final String expectedAdminToken;

        /**
         * Constructs the admin token filter.
         * 
         * @param expectedAdminToken the valid admin token from configuration
         */
        public AdminTokenAuthenticationFilter(String expectedAdminToken) {
            this.expectedAdminToken = expectedAdminToken;
        }

        /**
         * Processes each HTTP request to validate admin token if present.
         * 
         * @param request HTTP servlet request
         * @param response HTTP servlet response  
         * @param filterChain filter chain to continue processing
         * @throws ServletException if servlet processing fails
         * @throws IOException if I/O operation fails
         */
        @Override
        protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                        @NonNull HttpServletResponse response,
                                        @NonNull FilterChain filterChain) throws ServletException, IOException {
            
            String adminTokenHeader = request.getHeader("X-Admin-Token");
            
            if (adminTokenHeader != null && expectedAdminToken.equals(adminTokenHeader)) {
                // Create admin user with ADMIN role
                UserDetails adminUser = User.withUsername("admin")
                        .password("unused") // Password not used with token auth
                        .roles("ADMIN")
                        .build();
                
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        adminUser, null, adminUser.getAuthorities()
                    );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            
            filterChain.doFilter(request, response);
        }
    }
}