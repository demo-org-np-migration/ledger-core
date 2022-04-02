package io.cauri.ledger.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cauri.ledger.common.ApiError;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Resource-server config. Issuer comes from KEYCLOAK_ISSUER (application.yml).
 * Authorization follows the pattern the org uses across services: any valid
 * token gets in, but the ledger-writing surface and the KYC status transition
 * are restricted to service-to-service tokens (role "service").
 *
 * Note (supuesto): las convenciones internas de API's per-endpoint table lists
 * "GET /v1/ledger/entries?account_id=" as JWT (any token), but the coarser
 * rule we were given for this repo is "/v1/ledger/** requires role service".
 * We follow the coarser /v1/ledger/** rule for the whole controller,
 * including that GET, and call it out here and in the README.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter,
                                            ObjectMapper objectMapper) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/v1/customers/*/kyc-status").hasRole("service")
                        .requestMatchers("/v1/ledger/**").hasRole("service")
                        .requestMatchers("/v1/legacy-batch/**").hasRole("service")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, ex) ->
                                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, objectMapper))
                        .accessDeniedHandler((request, response, ex) ->
                                writeError(response, HttpServletResponse.SC_FORBIDDEN, objectMapper)));
        return http.build();
    }

    private void writeError(HttpServletResponse response, int status, ObjectMapper objectMapper) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String code = status == HttpServletResponse.SC_UNAUTHORIZED ? "unauthorized" : "forbidden";
        String message = status == HttpServletResponse.SC_UNAUTHORIZED
                ? "missing or invalid bearer token"
                : "token does not have the required role";
        response.getWriter().write(objectMapper.writeValueAsString(ApiError.of(code, message)));
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }
}
