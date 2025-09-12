package com.atlan.evently.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 configuration for Evently API documentation.
 * 
 * Provides comprehensive API documentation with:
 * - Security schemes for admin authentication
 * - Server information for different environments
 * - Contact and license information
 * - Interactive Swagger UI at /swagger-ui.html
 * 
 * @author Evently Team
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Value("${evently.api.version:1.0.0}")
    private String apiVersion;

    @Value("${evently.api.server-url:http://localhost:8080}")
    private String serverUrl;

    @Value("${evently.api.server-description:Development Server}")
    private String serverDescription;

    /**
     * Configures the OpenAPI specification for the Evently platform.
     * 
     * @return OpenAPI specification with comprehensive API documentation
     */
    @Bean
    public OpenAPI eventlyOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServerList())
                .components(createSecurityComponents())
                .addSecurityItem(createSecurityRequirement());
    }

    /**
     * Creates API information including title, description, version, and contact details.
     * 
     * @return API information metadata
     */
    private Info createApiInfo() {
        return new Info()
                .title("Evently Platform API")
                .description("""
                        # Evently - Enterprise Event Ticketing Platform
                        
                        ## Overview
                        A scalable backend system for event ticketing that handles concurrent bookings, 
                        prevents overselling, and provides comprehensive analytics.
                        
                        ## Features
                        - **Atomic Booking Operations**: Zero overselling with database-level concurrency control
                        - **Waitlist System**: FIFO queue with automatic notifications
                        - **Real-time Notifications**: Email, in-app, and WebSocket notifications
                        - **Event-driven Architecture**: Kafka-based messaging for scalability
                        - **Comprehensive Analytics**: Popular events, utilization rates, booking trends
                        - **Admin Management**: Complete event and user management capabilities
                        
                        ## Authentication
                        - **Public Endpoints**: Event browsing, user registration
                        - **Admin Endpoints**: Require `X-Admin-Token` header for authentication
                        - **User Endpoints**: Currently public for testing (add JWT in production)
                        
                        ## Rate Limiting
                        - Booking endpoints: 100 requests per minute per IP
                        - General API endpoints: 1000 requests per minute per IP
                        
                        ## Error Handling
                        All endpoints return structured error responses with:
                        - HTTP status code
                        - Error message
                        - Error code for programmatic handling
                        - Timestamp and request path
                        
                        ## Support
                        - API Issues: Create GitHub issue with reproduction steps
                        - Documentation: Available at `/swagger-ui.html`
                        - Health Check: Available at `/actuator/health`
                        """)
                .version(apiVersion)
                .contact(createContactInfo())
                .license(createLicenseInfo());
    }

    /**
     * Creates contact information for API support.
     * 
     * @return Contact information
     */
    private Contact createContactInfo() {
        return new Contact()
                .name("Evently Development Team")
                .email("support@evently.com")
                .url("https://github.com/evently/backend");
    }

    /**
     * Creates license information for the API.
     * 
     * @return License information
     */
    private License createLicenseInfo() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    /**
     * Creates server configuration for different environments.
     * 
     * @return List of server configurations
     */
    private List<Server> createServerList() {
        Server developmentServer = new Server()
                .url(serverUrl)
                .description(serverDescription);

        Server productionServer = new Server()
                .url("https://api.evently.com")
                .description("Production Server");

        return List.of(developmentServer, productionServer);
    }

    /**
     * Creates security components including authentication schemes.
     * 
     * @return Security components configuration
     */
    private Components createSecurityComponents() {
        SecurityScheme adminTokenScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-Admin-Token")
                .description("Admin token for administrative operations");

        SecurityScheme bearerTokenScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT token for user authentication (future implementation)");

        return new Components()
                .addSecuritySchemes("AdminToken", adminTokenScheme)
                .addSecuritySchemes("BearerAuth", bearerTokenScheme);
    }

    /**
     * Creates security requirements for API endpoints.
     * 
     * @return Security requirements configuration
     */
    private SecurityRequirement createSecurityRequirement() {
        return new SecurityRequirement()
                .addList("AdminToken");
    }
}
