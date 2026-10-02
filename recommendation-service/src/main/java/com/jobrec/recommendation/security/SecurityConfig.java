package com.jobrec.recommendation.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

@Configuration @EnableWebSecurity @EnableMethodSecurity
public class SecurityConfig {
    @Value("${jwt.secret}") private String secret;
    @Bean JwtDecoder jwtDecoder() { return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(secret.getBytes(), "HmacSHA256")).build(); }
    @Bean JwtAuthenticationConverter jwtAuthenticationConverter() { JwtAuthenticationConverter c = new JwtAuthenticationConverter(); c.setJwtGrantedAuthoritiesConverter(jwt -> { String role = jwt.getClaimAsString("role"); return role == null ? List.of() : List.of(new SimpleGrantedAuthority("ROLE_" + role)); }); return c; }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter converter) throws Exception { return http.cors(Customizer.withDefaults()).csrf(AbstractHttpConfigurer::disable).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(a -> a.anyRequest().authenticated()).oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(converter))).build(); }
}
