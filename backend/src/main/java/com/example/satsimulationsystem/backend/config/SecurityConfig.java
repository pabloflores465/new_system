package com.example.satsimulationsystem.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless APIs (if using JWT, otherwise configure properly)
            .authorizeHttpRequests(authz -> authz
                // Allow public access to actuator health (if needed)
                // .requestMatchers("/actuator/health").permitAll()
                // Define access rules for user module - to be refined
                .requestMatchers("/api/users/**").hasRole("ADMINISTRATOR") // Example: only ADMIN can access user endpoints
                .requestMatchers("/api/invoicing/reports/**").hasRole("ADMINISTRATOR") // Secure reporting endpoints
                .requestMatchers("/api/invoicing/orders/**").authenticated() // Module users can create orders
                .requestMatchers("/api/invoicing/invoices/download/**").authenticated() // Authenticated users can download their invoices (further checks might be needed if users should only download their own)
                .anyRequest().authenticated() // All other requests need authentication
            )
            .httpBasic(withDefaults()) // Use HTTP Basic authentication for now
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // Stateless session management

        return http.build();
    }

    // We might add UserDetailsService configuration later if we move away from in-memory/basic auth.
} 