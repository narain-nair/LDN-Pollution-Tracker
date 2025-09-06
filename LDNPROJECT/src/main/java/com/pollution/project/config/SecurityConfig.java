package com.pollution.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.pollution.project.service.PollutionUserDetailsService;

@Configuration
public class SecurityConfig {
    private final PollutionUserDetailsService userDetailsService;
    private final BCryptPasswordEncoder passwordEncoder;

    public SecurityConfig(PollutionUserDetailsService userDetailsService, BCryptPasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disables CSRF protection for simplicity
            .authorizeHttpRequests((authz) -> authz
                .requestMatchers("/auth/**").permitAll() // All /auth/** endpoints are public
                .anyRequest().authenticated() // All other endpoints require authentication
            )
            .userDetailsService(userDetailsService) // Loads users from the custom user details service
            .httpBasic(Customizer.withDefaults()); // Enables HTTP Basic authentication
        return http.build(); // Builds the SecurityFilterChain
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
