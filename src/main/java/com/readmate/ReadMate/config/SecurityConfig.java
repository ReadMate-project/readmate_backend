package com.readmate.ReadMate.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf((csrfConfig) ->
                        csrfConfig.disable()
                )
                .headers((headerConfig)->headerConfig.frameOptions(frameOptionsConfig -> frameOptionsConfig.disable()))
                .authorizeHttpRequests((authorizeRequests)-> authorizeRequests
                        .requestMatchers(URL_TO_PERMIT).permitAll()
                        .anyRequest().authenticated()
        );
        return http.build();
    }

    private static final String[] URL_TO_PERMIT = {
            "/member/login",
            "/member/signup",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/auth/**"
    };
    }
