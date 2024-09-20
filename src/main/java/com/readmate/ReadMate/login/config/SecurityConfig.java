package com.readmate.ReadMate.login.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
//                .headers(headers -> headers
//                        .frameOptions(frameOptions -> frameOptions.disable()) // Frame options 비활성화
//                )
//                .securityContext(securityContext -> securityContext
//                        .requireExplicitSave(false) // SecurityContext의 명시적 저장 비활성화
//                )
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Stateless 세션 정책
//                )
//                .authorizeHttpRequests(authz -> authz
//                        .requestMatchers(
//                                "/api/v1/**",
//                                "/api/token/**",
//                                "/v2/api-docs",
//                                "/v3/api-docs",
//                                "/v3/api-docs/**",
//                                "/swagger-resources",
//                                "/swagger-resources/**",
//                                "/configuration/ui",
//                                "/configuration/security",
//                                "/swagger-ui/**",
//                                "/webjars/**",
//                                "/swagger-ui.html"
//                        ).permitAll() // 허용된 요청 경로
//                        .anyRequest().authenticated() // 그 외의 요청은 인증 필요
//                )
//                .formLogin(form -> form.disable()) // Form login 비활성화
//                .cors(cors -> cors.configurationSource(corsConfigurationSource())); // CORS 설정
//
//        return http.build();
//    }
//
//
//
//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowCredentials(true);
//        config.addAllowedOriginPattern("*"); // 모든 도메인,헤더,HTTP메소드 허용
//        config.addAllowedHeader("*");
//        config.addAllowedMethod("*");
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }
//
////    @Bean
////    public PasswordEncoder passwordEncoder(){
////        return new BCryptPasswordEncoder(); //해시 암호화 알고리즘
////    }
//
//}
