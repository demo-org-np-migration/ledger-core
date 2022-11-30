package io.cauri.ledger.customers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ledger-core spec, "Tests mínimos": MockMvc + token falso. We mock the
 * JwtDecoder so this suite never talks to Keycloak (spec: "En los tests,
 * mockeá el JwtDecoder para no depender de Keycloak").
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void createCustomerWithAnyValidTokenReturns201() throws Exception {
        stubJwt("customer");

        mockMvc.perform(post("/v1/customers")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.tokenValue("fake-token")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"new.customer@cauri.io\",\"full_name\":\"Nueva Persona\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.kyc_status").value("pending"));
    }

    @Test
    void kycStatusChangeWithoutServiceRoleIsForbidden() throws Exception {
        stubJwt("customer");

        mockMvc.perform(patch("/v1/customers/{id}/kyc-status", java.util.UUID.randomUUID())
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.tokenValue("fake-token")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kyc_status\":\"approved\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("forbidden"));
    }

    private void stubJwt(String role) {
        Jwt jwt = Jwt.withTokenValue("fake-token")
                .header("alg", "none")
                .claim("sub", "test-subject")
                .claim("realm_access", Map.of("roles", java.util.List.of(role)))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
    }
}
