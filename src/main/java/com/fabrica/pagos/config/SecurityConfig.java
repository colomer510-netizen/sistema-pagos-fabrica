package com.fabrica.pagos.config;

import com.fabrica.pagos.model.Usuario;
import com.fabrica.pagos.repository.UsuarioRepository;
import com.fabrica.pagos.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UsuarioService usuarioService,
                                                           PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationFailureHandler failureHandler(UsuarioRepository usuarioRepository,
                                                      @Value("${app.security.max-intentos:5}") int maxIntentos,
                                                      @Value("${app.security.bloqueo-minutos:15}") long bloqueoMinutos) {
        return (request, response, exception) -> {
            String username = request.getParameter("username");
            String error = "error=true";
            if (username != null && !username.isBlank()) {
                Usuario u = usuarioRepository.findByUsername(username).orElse(null);
                if (u != null) {
                    LocalDateTime bloqueo = u.getFechaBloqueo();
                    if (bloqueo != null) {
                        if (LocalDateTime.now().isBefore(bloqueo.plusMinutes(bloqueoMinutos))) {
                            error = "error=bloqueado";
                        } else {
                            u.setFechaBloqueo(null);
                            u.setIntentosFallidos(0);
                            usuarioRepository.save(u);
                        }
                    } else if (Boolean.FALSE.equals(u.getActivo())) {
                        error = "error=inactivo";
                    } else {
                        int intentos = u.getIntentosFallidos() == null ? 0 : u.getIntentosFallidos();
                        intentos++;
                        u.setIntentosFallidos(intentos);
                        if (intentos >= maxIntentos) {
                            u.setFechaBloqueo(LocalDateTime.now());
                        }
                        usuarioRepository.save(u);
                    }
                }
            }
            response.sendRedirect(request.getContextPath() + "/login?" + error);
        };
    }

    @Bean
    public AuthenticationSuccessHandler successHandler(UsuarioRepository usuarioRepository) {
        return (request, response, authentication) -> {
            usuarioRepository.findByUsername(authentication.getName()).ifPresent(u -> {
                u.setIntentosFallidos(0);
                u.setFechaBloqueo(null);
                u.setFechaUltimoLogin(LocalDateTime.now());
                usuarioRepository.save(u);
            });
            response.sendRedirect(request.getContextPath() + "/");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationSuccessHandler successHandler,
                                           AuthenticationFailureHandler failureHandler) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/img/**", "/login", "/error", "/favicon.ico").permitAll()
                .requestMatchers("/api/**").permitAll()
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll());
        return http.build();
    }
}
