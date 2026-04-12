package com.sqltutor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig — Spring Security Configuration
 * Handles login, logout, session management, CSRF protection.
 * Replaces the old LoginServlet + SessionManagement code.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder enc) {
        return new InMemoryUserDetailsManager(
            User.withUsername("admin")
                .password(enc.encode("admin123")).roles("ADMIN","USER").build(),
            User.withUsername("student")
                .password(enc.encode("student123")).roles("USER").build(),
            User.withUsername("guest")
                .password(enc.encode("guest123")).roles("USER").build()
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("SQL_TUTOR_SESSION")
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
            );
        return http.build();
    }
}
