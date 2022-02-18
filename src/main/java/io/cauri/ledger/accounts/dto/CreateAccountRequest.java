package io.cauri.ledger.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAccountRequest(
        @NotNull UUID customerId,
        @NotBlank String currency) {
}
