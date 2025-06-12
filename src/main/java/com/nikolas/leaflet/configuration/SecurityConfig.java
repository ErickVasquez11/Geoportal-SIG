// SecurityConfig.java
package com.nikolas.leaflet.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/","/update","/index", "/map/index", "/register", "/map/unidades", "/map/informacion","/dialogflow/webhook", "/css/**", "/js/**", "/images/**", "/webjars/**")
                .permitAll()  // Permite acceso a estas rutas sin autenticación
                .anyRequest().authenticated()  // Requiere autenticación para el resto
            .and()
            .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/admin/clinicMedic", true)
                .failureUrl("/login?error=true")
                .permitAll()
            .and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/map/index")
                .permitAll()
            .and()
            .csrf().disable();

        return http.build();
    }

}
