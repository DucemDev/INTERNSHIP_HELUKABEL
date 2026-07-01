package com.helu.internship.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**", "/customer-value-matrix.html", "/css/customer-value-matrix.css", "/js/customer-value-matrix.js").permitAll()

                        // Cho chatbot Python gọi API dashboard không cần đăng nhập
                        .requestMatchers("/api/dashboard/**").permitAll()

                        .requestMatchers("/users/**").hasRole("Admin")
                        .requestMatchers("/customer-analysis").hasRole("Admin")
                        .requestMatchers("/dashboard/**").hasAnyRole("Admin", "Seller")
                        .requestMatchers("/seller/**").hasAnyRole("Seller")
                        .requestMatchers("/api/seller/**").hasRole("Seller")
                        .requestMatchers("/chatbot", "/api/chatbot/**").hasAnyRole("Admin", "Seller")

                        .anyRequest().authenticated()
                )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(successHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
