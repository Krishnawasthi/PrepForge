package com.prepforge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        config.setAllowCredentials(true);
        // Allow local frontend development and production origins
        config.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "http://localhost:3001",
            "https://*.vercel.app",
            "https://prepforge.app",
            "https://*.prepforge.app"
        ));
        config.setAllowedHeaders(Arrays.asList(
            "Origin", "Content-Type", "Accept", "Authorization", "X-Session-Id", "X-Requested-With"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setExposedHeaders(List.of("X-Total-Count", "X-Session-Id", "Retry-After"));
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
