package com.aaami.gateway.config;

import com.aaami.gateway.filter.RateLimitFilter;
import com.aaami.gateway.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/logout").authenticated()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                // .requestMatchers("/api/users").permitAll() // Allow user registration
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/users").hasAnyRole("USER", "PREMIUM_USER", "ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/users", "/api/users/{id}").hasRole("ADMIN")

                
                // Product GET endpoints - view allowed for all authenticated users
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/products", "/api/products/{id}").hasAnyRole("USER", "PREMIUM_USER", "ADMIN")
                
                // Product CRUD (POST, PUT, DELETE) - only ADMIN
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/products").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                
                // Order endpoints - USER and PREMIUM_USER can create orders, ADMIN can only view
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/orders").hasAnyRole("USER", "PREMIUM_USER")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/orders", "/api/orders/**").hasAnyRole("USER", "PREMIUM_USER", "ADMIN")
                // All other order operations (PUT, DELETE, etc.) - only USER and PREMIUM_USER
                .requestMatchers("/api/orders/**").hasAnyRole("USER", "PREMIUM_USER")
                
                // User endpoints - authenticated users can view their own data
                .requestMatchers("/api/users/**").hasAnyRole("USER", "PREMIUM_USER", "ADMIN")
                
                .anyRequest().authenticated()
            )
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}

