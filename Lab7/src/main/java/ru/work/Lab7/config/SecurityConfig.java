package ru.work.Lab7.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.work.Lab7.ExceptionHandlers.MyAccessDeniedException;
import ru.work.Lab7.filter.JwtRequestFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    // Список публичных эндпоинтов
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v2/login",
            "/api/v2/login/**",
            "/swagger-ui.html",       // ← Явно разрешаем HTML-страницу Swagger
            "/swagger-ui/**",        // Разрешаем все ресурсы Swagger UI (JS, CSS)
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/favicon.ico",
            "/api/v2/getStatus"
    };

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)  // Отключаем CSRF для REST API
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Без сессий
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll() // Разрешаем публичные эндпоинты
                        .anyRequest().authenticated() // Все остальное требует аутентификации
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, ex) -> {
                            // Пробрасываем AccessDeniedException в @ControllerAdvice
                            throw new MyAccessDeniedException("Access denied");
                        }));

        return http.build();
    }
}