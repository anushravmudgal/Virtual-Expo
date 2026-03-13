package com.ims.expo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

   @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable()) // Disabled for local development
            .authorizeHttpRequests(auth -> auth
                // THE FIX: Add /api/groups and the user endpoints to the public VIP list!
                .requestMatchers(
                    "/", 
                    "/api/groups", 
                    "/api/users",
                    "/api/groups/**",
                    "/api/admin/login", 
                    "/api/users/resume/download/**"

                ).permitAll() 
                
                .anyRequest().authenticated() // Everything else requires login
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2LoginSuccessHandler) // Triggers our gatekeeper logic!
            );

        return http.build();
    }

    // Allows your Angular frontend to communicate with Spring Boot
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // Angular dev server
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}