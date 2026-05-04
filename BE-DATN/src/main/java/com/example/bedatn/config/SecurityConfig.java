package com.example.bedatn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider authenticationProvider) throws Exception {
        http.authenticationProvider(authenticationProvider)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/files/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/lien-he").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/inquiries").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/inquiries/**").hasAnyRole("MANAGER", "STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/inquiries/**").hasAnyRole("MANAGER", "STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/support-chat/messages").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/support-chat/session").permitAll()
                        .requestMatchers("/api/support-chat/me/**").hasRole("USER")
                        .requestMatchers("/api/support-chat/admin/**").hasAnyRole("MANAGER", "STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/events/admin/list").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/events").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/events/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/events").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/events/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/dashboard/**").hasAnyRole("MANAGER", "STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/v1/legal-documents/expiring-soon").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/legal-documents/**").hasAnyRole("MANAGER", "STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/v1/legal-documents/buildings/*/upload").hasAnyRole("MANAGER", "STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/legal-documents/*/verify").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/legal-documents/*/reject").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/legal-documents/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/buildings/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/buildings/**").hasAnyRole("MANAGER", "STAFF")
                        .requestMatchers(HttpMethod.DELETE, "/api/buildings/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/customers/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/transactions").hasAnyRole("MANAGER", "STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/transactions/**").hasAnyRole("MANAGER", "STAFF")
                        .requestMatchers(HttpMethod.DELETE, "/api/transactions/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/user").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/user/**").hasAnyRole("MANAGER", "STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/user/*").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/user/password/*/reset").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/user").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/user/change-password/*", "/api/user/profile/*").hasAnyRole("USER", "STAFF", "MANAGER")
                        .requestMatchers("/api/assign", "/api/assignCustomer").hasRole("MANAGER")
                        // Một đoạn path (id) giữa resource và staffs — không dùng **/ ở giữa (PathPattern Spring 6+)
                        .requestMatchers("/api/buildings/*/staffs", "/api/customers/*/staffs").hasRole("MANAGER")
                        .requestMatchers("/api/customers/**").hasAnyRole("MANAGER", "STAFF")
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
