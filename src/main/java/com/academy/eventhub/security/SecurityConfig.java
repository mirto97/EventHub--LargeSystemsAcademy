package com.academy.eventhub.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth

                // endpoint pubblici
                .requestMatchers(HttpMethod.POST, "/auth/signup").permitAll()
                .requestMatchers(HttpMethod.GET, "/events", "/events/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/events/{id}/feedbacks").permitAll()
                .requestMatchers(HttpMethod.GET, "/events/{id}/rating").permitAll()
                .requestMatchers(HttpMethod.GET, "/venues", "/venues/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/tags").permitAll()
                .requestMatchers(HttpMethod.GET, "/speakers", "/speakers/{id}").permitAll()
                .requestMatchers("/swagger", "/apidocs/**", "/swagger-ui/**").permitAll()

                // solo ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // solo ORGANIZER (o ADMIN)
                .requestMatchers(HttpMethod.POST, "/events").hasAnyRole("ORGANIZER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/events/{id}").hasAnyRole("ORGANIZER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/events/{id}").hasAnyRole("ORGANIZER", "ADMIN")
                .requestMatchers("/events/my").hasAnyRole("ORGANIZER", "ADMIN")
                .requestMatchers("/events/{id}/participants").hasAnyRole("ORGANIZER", "ADMIN")

                // qualsiasi utente autenticato
                .requestMatchers("/me/**").authenticated()
                .requestMatchers("/events/{eventId}/book").authenticated()
                .requestMatchers("/tickets/**").authenticated()
                .requestMatchers("/feedbacks/**").authenticated()

                // tutto il resto richiede autenticazione
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}