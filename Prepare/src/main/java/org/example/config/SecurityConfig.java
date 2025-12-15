package org.example.config;

import org.example.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                    .antMatchers("/", "/css/**", "/js/**", "/login", "/register",
                            "/api/auth/register", "/api/auth/login").permitAll()
                    .antMatchers("/admin/**").hasRole("ADMIN")
                    .antMatchers("/orders/new", "/orders").hasRole("USER") // Только USER может создавать заказы
                    .antMatchers("/orders/admin/**").hasRole("ADMIN") // Админ видит все заказы
                    .anyRequest().authenticated()
                .and()
                    .formLogin()
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                .and()
                    .logout()
                        .logoutSuccessUrl("/login?logout")
                        .permitAll();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Для простоты используем нешифрованные пароли (можно заменить на BCrypt)
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}

