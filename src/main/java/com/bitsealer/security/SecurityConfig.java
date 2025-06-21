package com.bitsealer.security;

import com.bitsealer.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Configuración de Spring-Security:
 *  ─ UserDetailsService  ➜ autenticación/roles           (inyectado)
 *  ─ PasswordEncoder     ➜ comparar contraseñas           (inyectado)
 *  ─ SecurityFilterChain ➜ reglas de acceso a URL         (este fichero)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder   = passwordEncoder;
    }

    /* ─────────────────── Autenticación ─────────────────── */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder);
    }

    /* ─────────────────── Autorización ─────────────────── */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            /* 1)  CSRF desactivado para simplificar (en producción conviene habilitarlo) */
            .csrf(csrf -> csrf.disable())

            /* 2)  Reglas de acceso */
            .authorizeHttpRequests(auth -> auth

                /* 2-A  ─ Rutas públicas ───────────────────────────────── */
                .requestMatchers(
                        "/", "/home",                     // portada
                        "/login", "/register",            // auth públicas
                        "/css/**", "/js/**", "/img/**",   // recursos estáticos
                        "/vendor/**", "/webjars/**"       // librerías front (SB Admin 2, etc.)
                ).permitAll()

                /* 2-B  ─ Resto: requiere sesión ───────────────────────── */
                .anyRequest().authenticated()
            )

            /* 3)  Form-login */
            .formLogin(form -> form
                .loginPage("/login")           // tu plantilla personalizada
                .loginProcessingUrl("/login")  // acción <form>
                .defaultSuccessUrl("/", true)  // al loguearse vuelve a la home
                .permitAll()
            )

            /* 4)  Logout */
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}
