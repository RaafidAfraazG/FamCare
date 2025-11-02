package com.famcare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
            .csrf(csrf -> csrf.disable()) // Simplified for demo
            .authorizeHttpRequests(authz -> authz
                // Public pages
                .requestMatchers("/", "/index", "/login", "/register", "/register/**").permitAll()
                
                // Static resources
                .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**").permitAll()
                
                // Swagger UI (for API documentation demo)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // Protected pages (simple check - you handle role-based in controllers)
                .requestMatchers("/admin/**", "/parent/**", "/child/**").authenticated()
                
                // API endpoints
                .requestMatchers("/api/**").authenticated()
                
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}