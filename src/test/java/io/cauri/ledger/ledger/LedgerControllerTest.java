package io.cauri.ledger.ledger;

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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * las convenciones internas de API: GET /v1/ledger/entries?account_id= acepta cualquier token
 * válido (no hace falta rol service); las escrituras del ledger sí lo piden.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class LedgerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void listEntriesWithoutServiceRoleIsAllowed() throws Exception {
        stubJwt("customer");

        mockMvc.perform(get("/v1/ledger/entries")
                        .queryParam("account_id", UUID.randomUUID().toString())
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.tokenValue("fake-token"))))
                .andExpect(status().isOk());
    }

    @Test
    void createEntryWithoutServiceRoleIsForbidden() throws Exception {
        stubJwt("customer");

        mockMvc.perform(post("/v1/ledger/entries")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.tokenValue("fake-token")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"account_id\":\"" + UUID.randomUUID() + "\",\"amount\":\"10.00\",\"type\":\"credit\"}"))
                .andExpect(status().isForbidden());
    }

    private void stubJwt(String role) {
        Jwt jwt = Jwt.withTokenValue("fake-token")
                .header("alg", "none")
                .claim("sub", "test-subject")
                .claim("realm_access", Map.of("roles", List.of(role)))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
    }
}
