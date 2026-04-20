package com.example.medhub.configuration.security.jwt;

import com.example.medhub.enums.Authority;
import com.example.medhub.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class JwtSecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationService authenticationService;
    private final PasswordEncoder passwordEncoder;

    private final String ADMIN = Authority.ROLE_ADMIN.getAuthority();
    private final String WORKER = Authority.ROLE_WORKER.getAuthority();
    private final String PATIENT = Authority.ROLE_PATIENT.getAuthority();
    private final String DOCTOR = Authority.ROLE_DOCTOR.getAuthority();

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()

                        .requestMatchers("/v1/signin").permitAll()

                        .requestMatchers("/v1/invitations/**").permitAll()

                        .requestMatchers("/v1/users/signup").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/users").hasAuthority(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/v1/users/{id}").hasAnyAuthority(ADMIN)
                        .requestMatchers("/v1/users/**").hasAnyAuthority(ADMIN, PATIENT)

                        .requestMatchers(HttpMethod.POST, "/v1/workers/signup").hasAnyAuthority(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/v1/workers/doctor-location-requests").hasAnyAuthority(WORKER)
                        .requestMatchers("/v1/workers/**").hasAnyAuthority(ADMIN, WORKER)

                        .requestMatchers(HttpMethod.GET, "/v1/specializations/**").permitAll()
                        .requestMatchers("/v1/specializations/**").hasAnyAuthority(ADMIN, WORKER)

                        .requestMatchers(HttpMethod.DELETE, "/v1/locations/{id}").hasAnyAuthority(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/v1/locations").hasAnyAuthority(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/v1/locations/**").hasAnyAuthority(ADMIN, WORKER, PATIENT, DOCTOR)
                        .requestMatchers(HttpMethod.PATCH, "/v1/locations/{id}").hasAnyAuthority(ADMIN, WORKER)
                        .requestMatchers("/v1/locations/**").hasAnyAuthority(ADMIN, WORKER)

                        .requestMatchers(HttpMethod.POST, "/v1/doctors/signup").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/doctors/**").hasAnyAuthority(ADMIN, WORKER, PATIENT, DOCTOR)
                        .requestMatchers("/v1/doctors/**").hasAnyAuthority(ADMIN, WORKER)

                        .requestMatchers("/v1/doctor/**").hasAnyAuthority(DOCTOR)

                        .requestMatchers("/v1/facility/**").hasAnyAuthority(WORKER)

                        .requestMatchers(HttpMethod.GET, "/v1/availability/**").hasAnyAuthority(ADMIN, WORKER, PATIENT)
                        .requestMatchers("/v1/availability/**").hasAnyAuthority(ADMIN, WORKER)

                        .requestMatchers(HttpMethod.POST, "/v1/symptom-checker").hasAnyAuthority(PATIENT)

                        .requestMatchers(HttpMethod.PATCH, "/v1/appointments/{id}")
                        .hasAnyAuthority(PATIENT)
                        .requestMatchers(HttpMethod.PATCH, "/v1/appointments/{id}/cancel")
                        .hasAnyAuthority(PATIENT)
                        .requestMatchers(HttpMethod.PATCH, "/v1/appointments/{id}/reschedule")
                        .hasAnyAuthority(PATIENT)
                        .requestMatchers("/v1/appointments/**").hasAnyAuthority(ADMIN, WORKER)

                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(authenticationService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
