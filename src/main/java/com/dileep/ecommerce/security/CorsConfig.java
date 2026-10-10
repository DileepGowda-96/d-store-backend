package com.dileep.ecommerce.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Allow local and deployed D Store frontends
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "https://d-store-frontend.vercel.app",
                "https://d-store-frontend-git-master-dileepgowda-96.vercel.app"
        ));

        // Allow required HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        // Allow required request headers
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin"
        ));

        // Allow credentials when required
        configuration.setAllowCredentials(true);

        // Cache preflight responses for one hour
        configuration.setMaxAge(3600L);

        // Register CORS configuration for all endpoints
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}